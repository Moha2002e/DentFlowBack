# DentFlowBack

API Spring Boot de DentFlow. Supabase est utilise uniquement comme base PostgreSQL ; l'inscription, BCrypt, les roles et les JWT sont geres par Spring Security.

## Configuration locale

La configuration d'execution IntelliJ doit contenir :

```text
SUPABASE_DB_PASSWORD=<mot-de-passe-postgresql>
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
POST /api/auth/register   Inscription et creation d'un JWT
POST /api/auth/login      Connexion et creation d'un JWT
GET  /api/auth/me         Profil courant, JWT obligatoire
```

Le frontend autorise par CORS est `http://localhost:4200` par defaut.

## Verification

```powershell
.\mvnw.cmd test
```
