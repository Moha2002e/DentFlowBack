# DentFlowBack

API Spring Boot de DentFlow. La base PostgreSQL tourne en local dans Docker ; l'inscription, BCrypt, les roles et les JWT sont geres par Spring Security.

## Base de donnees locale

La base PostgreSQL tourne dans Docker. Docker Desktop doit etre demarre, puis :

```powershell
docker compose up -d
```

La base est alors disponible sur `localhost:5432` (base `dentflow`, utilisateur `dentflow`, mot de passe `dentflow`), et ce sont deja les valeurs par defaut de `application.properties` : aucune variable d'environnement n'est necessaire pour lancer l'application depuis IntelliJ.

Commandes utiles :

```powershell
docker compose ps        # etat du conteneur
docker compose logs -f   # journaux PostgreSQL
docker compose stop      # arreter sans perdre les donnees
docker compose down -v   # supprimer le conteneur ET les donnees
```

Les donnees sont conservees dans le volume Docker `dentflowback_dentflow-db-data`.

## Configuration locale

Aucune variable n'est obligatoire : les valeurs par defaut pointent vers la base Docker et un secret JWT de developpement.

Pour pointer vers une autre base (Supabase, prod...), definir dans la configuration d'execution :

```text
DB_URL=jdbc:postgresql://<hote>:5432/<base>?sslmode=require
DB_USERNAME=<utilisateur>
DB_PASSWORD=<mot-de-passe>
JWT_SECRET=<secret-base64-de-32-octets-minimum>
```

Pour generer un secret JWT dans PowerShell :

```powershell
$jwtBytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($jwtBytes)
[Convert]::ToBase64String($jwtBytes)
```

Copier uniquement le resultat dans la variable `JWT_SECRET`. Ne pas enregistrer ce secret dans Git.

## Endpoints

```text
POST   /api/auth/register              Inscription et creation d'un JWT
POST   /api/auth/login                 Connexion et creation d'un JWT
GET    /api/auth/me                    Profil courant, JWT obligatoire

GET    /api/patients                   Liste  (?search=, ?activeOnly=)   ADMIN, DENTIST
GET    /api/patients/{id}              Fiche patient                     ADMIN, DENTIST
GET    /api/patients/{id}/record       Dossier : fiche + historique + compteurs
POST   /api/patients                   Creation                          ADMIN, DENTIST
PUT    /api/patients/{id}              Modification                      ADMIN, DENTIST
DELETE /api/patients/{id}              Archivage (historique conserve)   ADMIN, DENTIST
POST   /api/patients/{id}/restore      Reactivation                      ADMIN, DENTIST

GET    /api/dentists                   Liste  (?search=, ?activeOnly=)   authentifie
GET    /api/dentists/{id}              Fiche dentiste                    authentifie
POST   /api/dentists                   Creation                          ADMIN
PUT    /api/dentists/{id}              Modification                      ADMIN
DELETE /api/dentists/{id}              Desactivation                     ADMIN
POST   /api/dentists/{id}/activate     Reactivation                      ADMIN

GET    /api/appointments               Liste  (?patientId=, ?dentistId=, ?status=)
GET    /api/appointments/stats         Compteurs du planning
GET    /api/appointments/{id}          Detail
POST   /api/appointments               Prise de rendez-vous
PUT    /api/appointments/{id}          Modification / report
POST   /api/appointments/{id}/cancel   Annulation avec motif
POST   /api/appointments/{id}/complete Cloture

GET    /api/treatments                 Catalogue des soins actifs

GET    /api/users                      Gestion des comptes               ADMIN
```

### Repartition des responsabilites

Tout le traitement des donnees est fait par le backend : recherche, filtres,
compteurs, calculs (age, total facture, duree des creneaux), detection de
chevauchement de rendez-vous et droits d'action (`canEdit`, `canCancel`,
`canComplete` renvoyes avec chaque rendez-vous). Le frontend se contente
d'envoyer les parametres et d'afficher les reponses : il ne filtre, ne trie ni
ne recalcule rien.

### Regles metier des rendez-vous

- Un creneau qui chevauche un rendez-vous actif du meme dentiste est refuse.
- La duree reprend celle du soin choisi si elle n'est pas fournie, 30 min sinon.
- Un rendez-vous annule ne peut etre ni modifie, ni annule, ni cloture.
- Un rendez-vous realise ne peut plus etre annule.
- Deplacer un rendez-vous planifie le fait passer au statut `RESCHEDULED`.
- Patients et dentistes ne sont jamais supprimes, seulement archives.

### Donnees de demonstration

Au premier demarrage sur une base vide, l'application cree un compte
administrateur `admin@admin.com` / `admin123`, un catalogue de soins, des
dentistes, des patients et des rendez-vous. Chaque bloc ne s'execute que si sa
table est vide : relancer ne cree pas de doublon. Desactivable avec
`app.seed.enabled=false`.

## Verification

```powershell
.\mvnw.cmd test
```
