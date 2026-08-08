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
                echo '========== DEPLOYING CMMS FROM AWS ECR =========='

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
                        ECR_REGISTRY="135692633479.dkr.ecr.ap-south-1.amazonaws.com"
                        ECR_REPOSITORY="cmms-app"
                        IMAGE="$ECR_REGISTRY/$ECR_REPOSITORY:${BUILD_NUMBER}"

                        echo "=========================================="
                        echo "NEW IMAGE:"
                        echo "$IMAGE"
                        echo "=========================================="

                        # --------------------------------------------------
                        # STEP 1: Remember currently running image
                        # --------------------------------------------------

                        PREVIOUS_IMAGE=""

                        if docker ps -a --format '{{.Names}}' | grep -q '^cmms-app$'; then
                            PREVIOUS_IMAGE=$(docker inspect cmms-app \
                                --format '{{.Config.Image}}' 2>/dev/null || true)
                        fi

                        echo "Previous running image:"
                        echo "${PREVIOUS_IMAGE:-NONE}"

                        # --------------------------------------------------
                        # STEP 2: Login to ECR
                        # --------------------------------------------------

                        echo "Logging in to AWS ECR..."

                        aws ecr get-login-password --region ap-south-1 | \
                        docker login \
                            --username AWS \
                            --password-stdin $ECR_REGISTRY

                        # --------------------------------------------------
                        # STEP 3: Pull new image
                        # --------------------------------------------------

                        echo "Pulling new image from ECR..."

                        docker pull $IMAGE

                        # --------------------------------------------------
                        # STEP 4: Stop old container
                        # --------------------------------------------------

                        echo "Stopping old CMMS container..."

                        docker rm -f cmms-app 2>/dev/null || true

                        # --------------------------------------------------
                        # STEP 5: Start new container
                        # --------------------------------------------------

                        echo "Starting new CMMS container..."

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
                          $IMAGE

                        # --------------------------------------------------
                        # STEP 6: Health check new deployment
                        # --------------------------------------------------

                        echo "=========================================="
                        echo "CHECKING NEW DEPLOYMENT HEALTH"
                        echo "=========================================="

                        HEALTHY=false

                        for i in $(seq 1 12); do

                            echo "Health check attempt $i/12..."

                            if curl -fs http://localhost:8080/actuator/health | \
                               grep -q '"status":"UP"'; then

                                HEALTHY=true

                                echo "=========================================="
                                echo "NEW CMMS VERSION IS HEALTHY"
                                echo "=========================================="

                                break
                            fi

                            echo "Application not ready yet."
                            echo "Waiting 5 seconds..."

                            sleep 5
                        done

                        # --------------------------------------------------
                        # STEP 7: SUCCESS
                        # --------------------------------------------------

                        if [ "$HEALTHY" = "true" ]; then

                            echo "=========================================="
                            echo "CMMS DEPLOYMENT SUCCESSFUL"
                            echo "Health Check: UP"
                            echo "Image: $IMAGE"
                            echo "=========================================="

                            docker ps --filter name=cmms-app

                            exit 0
                        fi

                        # --------------------------------------------------
                        # STEP 8: NEW VERSION FAILED
                        # --------------------------------------------------

                        echo "=========================================="
                        echo "NEW CMMS VERSION FAILED HEALTH CHECK"
                        echo "=========================================="

                        echo "Failed image:"
                        echo "$IMAGE"

                        echo "Last 100 lines of failed container logs:"
                        docker logs --tail 100 cmms-app || true

                        # --------------------------------------------------
                        # STEP 9: Rollback
                        # --------------------------------------------------

                        if [ -z "$PREVIOUS_IMAGE" ]; then

                            echo "=========================================="
                            echo "ROLLBACK NOT POSSIBLE"
                            echo "No previous running image was found."
                            echo "=========================================="

                            exit 1
                        fi

                        echo "=========================================="
                        echo "STARTING AUTOMATIC ROLLBACK"
                        echo "=========================================="

                        echo "Previous image:"
                        echo "$PREVIOUS_IMAGE"

                        echo "Removing failed container..."

                        docker rm -f cmms-app 2>/dev/null || true

                        echo "Pulling previous image from ECR..."

                        docker pull "$PREVIOUS_IMAGE"

                        echo "Starting previous CMMS version..."

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
                          "$PREVIOUS_IMAGE"

                        # --------------------------------------------------
                        # STEP 10: Rollback health check
                        # --------------------------------------------------

                        echo "=========================================="
                        echo "CHECKING ROLLBACK HEALTH"
                        echo "=========================================="

                        ROLLBACK_HEALTHY=false

                        for i in $(seq 1 12); do

                            echo "Rollback health check attempt $i/12..."

                            if curl -fs http://localhost:8080/actuator/health | \
                               grep -q '"status":"UP"'; then

                                ROLLBACK_HEALTHY=true

                                echo "Previous version is HEALTHY."

                                break
                            fi

                            echo "Previous version not ready yet."
                            echo "Waiting 5 seconds..."

                            sleep 5
                        done

                        # --------------------------------------------------
                        # STEP 11: Rollback successful
                        # --------------------------------------------------

                        if [ "$ROLLBACK_HEALTHY" = "true" ]; then

                            echo "=========================================="
                            echo "AUTOMATIC ROLLBACK SUCCESSFUL"
                            echo "=========================================="

                            echo "Restored image:"
                            echo "$PREVIOUS_IMAGE"

                            docker ps --filter name=cmms-app

                            exit 1
                        fi

                        # --------------------------------------------------
                        # STEP 12: Rollback also failed
                        # --------------------------------------------------

                        echo "=========================================="
                        echo "CRITICAL: ROLLBACK FAILED"
                        echo "=========================================="

                        echo "Current container logs:"

                        docker logs --tail 100 cmms-app || true

                        exit 1
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
