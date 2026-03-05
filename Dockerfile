FROM openjdk:17.0.2-jdk

WORKDIR /app

COPY . ./

RUN mvn -B -DskipTests clean package

EXPOSE 8080

CMD ["sh", "-c", "java -jar target/*.jar"]
