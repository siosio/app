pipeline {
  agent {
    label 'maven'
  }
  stages {
    stage('Build') {
      steps {
        git branch: 'master', url: 'https://github.com/siosio/app.git'
        sh 'mvn install -DskipTests=true -Dmaven.repo.remote=${MAVEN_MIRROR_URL}'
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
    
    stage('Create DEV') {
      when {
        expression {
          openshift.withCluster() {
            openshift.withProject(env.DEV_PROJECT) {
              return !openshift.selector('dc', 'siosio-test-app').exists()
            }
          }
        }
      }
      steps {
        
        script {
          openshift.withCluster() {
            openshift.withProject(env.DEV_PROJECT) {
              def app = openshift.newApp("siosio-test-app:latest")
              app.narrow("svc").expose();
              openshift.set("probe dc/siosio-test-app --readiness --get-url=http://:8080 --initial-delay-seconds=30 --failure-threshold=10 --period-seconds=10")
              openshift.set("probe dc/siosio-test-app --liveness  --get-url=http://:8080 --initial-delay-seconds=180 --failure-threshold=10 --period-seconds=10")
              openshift.set("env dc/siosio-test-app --from=secrets/keel-postgresql")
              def dc = openshift.selector("dc", "siosio-test-app")
              def count = 0
              while (dc.object().spec.replicas != dc.object().status.availableReplicas) {
                if (count++ < 10) {
                  sleep 10
                } else {
                  throw new IllegalStateException("failed to create dev")
                }
              }
              openshift.set("triggers", "dc/siosio-test-app", "--manual")
            }
          }
        }
      }
    }
    stage('Deploy DEV') {
      steps {
        script {
          openshift.withCluster() {
            openshift.withProject(env.DEV_PROJECT) {
              openshift.selector("dc", "siosio-test-app").rollout().latest();
            }
          }
        }
      }
    }
  }
}
