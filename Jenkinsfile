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
