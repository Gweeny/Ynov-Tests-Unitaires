Feature: Gestion des livres
  Scenario: L'utilisateur crée un livre et le retrouve dans la liste
    Given l'utilisateur crée le livre "L'Anomalie" écrit par "Hervé Le Tellier"
    When l'utilisateur récupère la liste des livres
    Then la liste doit contenir le livre suivant
      | titre      | auteur           |
      | L'Anomalie | Hervé Le Tellier |