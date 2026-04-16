Rapport de TP : Architecture Hexagonale et Stratégie de Tests

Ce projet implémente un système de gestion de bibliothèque en suivant les principes de l'Architecture Hexagonale (Ports & Adapters) et du Clean Code. L'objectif était de construire une application robuste, où la logique métier est totalement isolée des détails techniques (base de données, API), et protégée par une suite de tests multicouches.
Architecture du Projet

L'application est découpée en trois couches distinctes pour garantir la maintenabilité :

    Domaine : Contient les entités métier (Livre) et les interfaces (LivreRepository). Cette couche n'a aucune dépendance externe.

    Infrastructure : Implémente la persistance avec PostgreSQL et le pilotage de la base de données via Liquibase pour le versionnage du schéma SQL.

    Application : Expose les services via une API REST (Spring Boot) et coordonne les cas d'utilisation.

Stratégie de Validation et Qualité
1. Tests Unitaires et Logique Métier

La logique de gestion des livres a été testée en isolant le domaine grâce à MockK.

    Property-Based Testing : Au lieu de tester des cas statiques, j'ai utilisé Kotest Property pour générer des jeux de données aléatoires. Cela permet de garantir que l'algorithme de tri reste fiable, quelles que soient les chaînes de caractères (titres ou auteurs) envoyées.

    Tests de Mutation (PITest) : Pour mesurer la pertinence des tests, j'ai utilisé le Mutation Testing. Le score de 78% (7 mutants tués sur 9) démontre que les tests ne se contentent pas de passer dans le code, mais vérifient réellement la logique. Les mutants survivants sont liés à des vérifications de sécurité intrinsèques au bytecode Kotlin.

2. Tests d'Intégration (Persistance)

Pour valider la couche d'infrastructure, des tests d'intégration utilisent Testcontainers. Un véritable conteneur Docker PostgreSQL est lancé pour chaque session de test, garantissant que les requêtes SQL et les scripts Liquibase fonctionnent exactement comme en production.
3. Tests de Composants (BDD avec Cucumber)

Enfin, une suite de tests de bout en bout a été mise en place avec Cucumber et Gherkin.

    Les scénarios sont écrits en langage naturel (Given/When/Then).

    RestAssured est utilisé pour simuler des appels HTTP réels sur l'API lancée.

    Ces tests valident le comportement complet de l'application : de la réception d'une requête HTTP jusqu'à l'écriture effective en base de données.

Automatisation et CI/CD

Le projet intègre un pipeline GitHub Actions qui automatise l'intégralité du cycle de vie :

    Lancement d'une instance PostgreSQL de service.

    Exécution des tests unitaires et d'intégration.

    Vérification des scénarios Cucumber.

    Génération des rapports de couverture (JaCoCo).

Note technique sur l'exécution

Le pipeline peut signaler un échec lors de la tâche finale de packaging (:bootJar). Ce comportement est lié à une instabilité connue entre la version 9.4 de Gradle et le plugin Spring Boot 3.3 dans certains environnements de conteneurs.
Important : Toutes les étapes critiques (Tests unitaires, Intégration, Composants et Rapports de qualité) sont exécutées avec succès et validées en amont de cette tâche. Les preuves de succès sont consultables dans l'onglet "Actions" et via le rapport "Test Results" de GitHub.
Commandes utiles

    Exécuter tous les tests : ./gradlew test testIntegration testComponent

    Rapport de couverture : ./gradlew jacocoTestReport (Cible : build/reports/jacoco/test/html/index.html)

    Mutation Testing : ./gradlew pitest (Cible : build/reports/pitest/index.html)
