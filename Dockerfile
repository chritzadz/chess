FROM openjdk:17.0.2-jdk

WORKDIR /app

COPY . ./

RUN ./mvnw -DoutputFile=target/mvn-dependency-list.log -B -DskipTests clean dependency:list install

EXPOSE 8080

CMD ["sh", "-c", "java -jar target/*.jar"]
