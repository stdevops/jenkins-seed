use(conjur.Conventions) {
  def job = job('evoke') {
    description('Build and test evoke, the configuration and server management tool.')

    steps {
      shell('./jenkins.sh')
    }

    publishers {
      archiveJunit('spec/reports/*.xml, features/reports/*.xml')
      postBuildScripts {
          steps {
              shell('./publish.sh')
          }
          onlyIfBuildSucceeds(true)
      }
    }
  }

  job.applyCommonConfig()
  job.addGitRepo('git@github.com:conjurinc/evoke.git')
}
