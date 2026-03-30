# ERD Generation (SchemaSpy)

`docs/erd/schemaspy.properties` is aligned with local defaults from `application-local.yml`.

## Prerequisite
- Run backend once with local profile so JPA creates/updates tables:
  - `set SPRING_PROFILES_ACTIVE=local && .\gradlew.bat bootRun`

## One-line command (PowerShell)
```powershell
docker run --rm -v "${PWD}\docs\erd:/output" -v "${PWD}\docs\erd\schemaspy.properties:/config/schemaspy.properties" schemaspy/schemaspy:latest -configFile /config/schemaspy.properties
```

## Result
- Open `docs/erd/index.html`

## If DB settings differ
- Update `docs/erd/schemaspy.properties` values:
  - `host`, `port`, `db`, `s`, `u`, `p`
