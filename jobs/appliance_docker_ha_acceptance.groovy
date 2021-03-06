use(conjur.Conventions) {
  def job = job('appliance-docker-ha-acceptance') {
    description('Run HA acceptance tests on Docker Conjur')

    parameters {
      stringParam('APPLIANCE_IMAGE', 'registry.tld/conjur-appliance', 'Appliance image id to test. Required.')
      stringParam('APPLIANCE_IMAGE_TAG', 'latest', 'Appliance image tag to test.')
    }

    concurrentBuild()
    throttleConcurrentBuilds {
      maxPerNode(1)
      maxTotal(2)
    }

    wrappers {
      rvm('2.1.5@appliance-docker-ha-acceptance')
    }

    steps {
      shell('''
        #!/bin/bash -e

        gem install bundler:1.11.2 && bundle install

        bootstrap_id=$(./ci/bin/ha-bootstrap --log-level debug -i $APPLIANCE_IMAGE -t $APPLIANCE_IMAGE_TAG)
        ./ci/bin/ha-fixtures --log-level debug $bootstrap_id
        echo "Bootstrap id: $bootstrap_id"
        rm -f ci/output/ha-acceptance/*
        ./ci/bin/ha-acceptance -l --log-level debug $bootstrap_id || true
      '''.stripIndent())
    }

    publishers {
      archiveJunit('ci/output/report/ha-acceptance/*.xml')
      archiveArtifacts('ci/output/**')
    }
  }

  job.addGitRepo('git@github.com:conjurinc/appliance.git', false)
  job.applyCommonConfig()
  job.setBuildName('#${BUILD_NUMBER} ${GIT_BRANCH}: ${ENV,var="APPLIANCE_IMAGE_TAG"}')
}
