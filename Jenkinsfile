	pipeline {
    agent any

    environment {
        SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/cmms_db'
        SPRING_DATASOURCE_USERNAME = 'postgres'
        CMMS_SEED_ENABLED = 'true'
    }

    stages {

        stage('Checkout') {
            steps {
                echo '========== CHECKOUT SOURCE CODE =========='
                checkout scm
            }
        }

        stage('Environment Check') {
            steps {
                sh '''
                    echo "========== JAVA =========="
                    java -version

                    echo "========== MAVEN =========="
                    mvn -version

                    echo "========== GIT =========="
                    git --version
                '''
            }
        }

        stage('Maven Test') {
            steps {
                echo '========== RUNNING TESTS =========='

                withCredentials([
                    string(
                        credentialsId: 'cmms-db-password',
                        variable: 'SPRING_DATASOURCE_PASSWORD'
                    )
                ]) {
                    sh 'mvn clean test'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo '========== SONARQUBE ANALYSIS =========='

                withSonarQubeEnv('SonarQube-CMMS') {
                    sh '''
                        mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                          -Dsonar.projectKey=CMMS-Spring-Boot \
                          -Dsonar.projectName="CMMS Spring Boot"
                    '''
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                echo '========== SONARQUBE QUALITY GATE =========='

                timeout(time: 5, unit: 'MINUTES') {
                waitForQualityGate abortPipeline: true
                 }
             }
         }

        stage('Maven Package') {
            steps {
                echo '========== BUILDING JAR =========='
                sh 'mvn package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                echo '========== BUILDING DOCKER IMAGE =========='

                  sh '''
                       docker build -t cmms-app:${BUILD_NUMBER} .
                       docker tag cmms-app:${BUILD_NUMBER} cmms-app:latest

                       echo "Docker image created successfully"
                       docker images | grep cmms-app
                     '''
                 }
          }

        stage('Push Image to ECR') {
            steps {
                echo '========== PUSHING IMAGE TO AWS ECR =========='

                sh '''
                    ECR_REGISTRY="135692633479.dkr.ecr.ap-south-1.amazonaws.com"
                    ECR_REPOSITORY="cmms-app"

                    echo "Logging in to Amazon ECR..."
                    aws ecr get-login-password --region ap-south-1 | \
                    docker login --username AWS --password-stdin $ECR_REGISTRY

                    echo "Tagging Docker images..."
                    docker tag cmms-app:${BUILD_NUMBER} \
                      $ECR_REGISTRY/$ECR_REPOSITORY:${BUILD_NUMBER}

                    docker tag cmms-app:${BUILD_NUMBER} \
                      $ECR_REGISTRY/$ECR_REPOSITORY:latest

                    echo "Pushing build ${BUILD_NUMBER}..."
                    docker push $ECR_REGISTRY/$ECR_REPOSITORY:${BUILD_NUMBER}

                    echo "Pushing latest..."
                    docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest

                    echo "ECR push completed successfully."
                '''
            }
        }

        stage('Deploy CMMS') {
            steps {
                echo '========== DEPLOYING CMMS =========='

                    withCredentials([
                        string(
                            credentialsId: 'cmms-db-password',
                            variable: 'DB_PASSWORD'
                             ),
                        string(
                            credentialsId: 'cmms-jwt-secret',
                            variable: 'JWT_SECRET_VALUE'
                            )
                    ]) {
                   sh '''
                       echo "Deploying Docker image: cmms-app:${BUILD_NUMBER}"

                       docker rm -f cmms-app 2>/dev/null || true

                       docker run -d \
                       --name cmms-app \
                       --restart unless-stopped \
                       --network cmms-springboot_default \
                       -p 8080:8080 \
                       -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/cmms_db \
                       -e SPRING_DATASOURCE_USERNAME=postgres \
                       -e SPRING_DATASOURCE_PASSWORD="$DB_PASSWORD" \
                       -e JWT_SECRET="$JWT_SECRET_VALUE" \
                       -e JWT_EXPIRATION_MS=86400000 \
                       -e CMMS_SEED_ENABLED=true \
                       cmms-app:${BUILD_NUMBER}

                      echo "Waiting for CMMS application..."
                          sleep 15

                      docker ps --filter name=cmms-app
                    '''
                     }
                } 
          }

        stage('Archive Artifact') {
            steps {
                echo '========== ARCHIVING ARTIFACT =========='

                archiveArtifacts artifacts: 'target/*.jar',
                                 fingerprint: true
            }
        }
    }

    post {
        success {
            echo '''
            ==========================================
                 CMMS CI PIPELINE SUCCESS
            ==========================================
            Checkout        : SUCCESS
            Maven Test      : SUCCESS
            SonarQube       : SUCCESS
            Maven Package   : SUCCESS
            Artifact        : ARCHIVED
            ==========================================
            '''
        }

        failure {
            echo '''
            ==========================================
                 CMMS CI PIPELINE FAILED
            ==========================================
            Check the failed stage in Console Output.
            ==========================================
            '''
        }

        always {
            echo 'CMMS Jenkins Pipeline execution completed.'
        }
    }
}
