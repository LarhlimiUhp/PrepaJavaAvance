# Utilisation d'une image légère car la compilation se fait sur l'hôte
FROM eclipse-temurin:17-jre
WORKDIR /app

# On copie le JAR de l'application
COPY target/gestion-stock-1.0-SNAPSHOT.jar app.jar

# On copie toutes les dépendances (dont le connecteur MySQL)
COPY target/lib /app/target/lib

# On copie les classes compilées si nécessaire
COPY target/classes /app/target/classes

# Exécution
ENTRYPOINT ["java", "-cp", "app.jar:target/lib/*", "com.examen.stock.MainApp"]
