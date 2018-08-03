pipeline {
  agent {
    label 'maven'
  }
  stages {
    stage('Build') {
      steps {
        git branch: 'master', url: 'https://github.com/siosio/app.git'
        sh 'mvn install -DskipTests=true'
      }
    }
    stage('Test') {
      steps {
        sh 'echo "なにもしない"'
      }
    }
    stage('Create Image Builder') {
      when {
        expression {
          openshift.withCluster() {
            openshift.withProject(env.DEV_PROJECT) {
              return !openshift.selector("bc", "siosio-test-app").exists();
            }
          }
        }
      }
      steps {
        script {
          openshift.withCluster() {
            openshift.withProject(env.DEV_PROJECT) {
              openshift.newBuild("--name=siosio-test-app", "--image-stream=openshift/java:8", "--binary=true")
            }
          }
        }
      }
    }
    stage('Build Image') {
      steps {
        sh "rm -rf oc-build && mkdir -p oc-build/deployments"
        sh "cp target/app*.jar oc-build/deployments/"

        script {
          openshift.withCluster() {
            openshift.withProject(env.DEV_PROJECT) {
              openshift.selector("bc", "siosio-test-app").startBuild("--from-dir=oc-build", "--wait=true")
            }
          }
        }
      }
    }
  }
}
