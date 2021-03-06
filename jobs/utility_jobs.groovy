def folderName = '__utilities'

folder(folderName) {
  description('Utility jobs, run on a timer')
}

job("${folderName}/cleanup_docker") {
  description('Periodically removes stopped containers and dangling images')
  logRotator(-1, 30, -1, 30)
  concurrentBuild()

  parameters {
    labelParam('NODE_LABEL') {
      defaultValue('docker')
      description('Run job on all nodes with this label')
      allNodes('allCases', 'IgnoreOfflineNodeEligibility')
    }
  }

  wrappers {
    preBuildCleanup()
    colorizeOutput()
    timestamps()
  }

  triggers {
    cron('H 5 * * *') // 5am UTC, 12am EST
  }

  steps {
    shell('''
    df -h

    echo '----------'
    
    docker ps -a -q --filter status=exited | xargs -r docker rm
    docker images -q --filter dangling=true | xargs -r  docker rmi || true
    docker volume ls -q --filter dangling=true | xargs -r docker volume rm

    image=conjurinc/docker-cleanup
    docker pull $image &> /dev/null

    docker run --rm \
      -v /var/run/docker.sock:/var/run/docker.sock \
      $image \
      --older-than 2.weeks \
      --match conjur- --match cuke- --match acceptance-ui | xargs -r docker rmi

    echo '----------'

    df -h
    '''.stripIndent())
  }
}
