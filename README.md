# Command Wheel (Fabric, Minecraft 26.2)

Ett kommandohjul för Minecraft. Håll inne tangenten (standard: **R**), välj en ruta och släpp.
Rutorna kan vara kommandon eller mappar (till exempel **Homes** med dina egna homes i).

Modden finns bara på klienten, så den fungerar på vilken server som helst.

## Bygga .jar-filen

### Alternativ A: GitHub (ingen installation)
1. Skapa ett nytt repo på github.com och ladda upp allt i den här mappen (inklusive `.github`).
2. Gå till fliken **Actions**, öppna bygget **build** och ladda ner **commandwheel-jar** när det är klart.
3. Zip-filen innehåller `commandwheel-1.0.0.jar`.

### Alternativ B: lokalt
1. Installera Java 25.
2. Kör `./gradlew build` (Windows: `gradlew.bat build`) i den här mappen.
3. Filen hamnar i `build/libs/commandwheel-1.0.0.jar`. Använd **inte** `-sources.jar`.

## Installera
1. Fabric Loader 0.19.5 eller nyare för Minecraft 26.2.
2. Fabric API för 26.2 i `mods`-mappen.
3. `commandwheel-1.0.0.jar` i `mods`-mappen.

## Användning
- När du joinar första gången visas en ruta (på engelska) där du väljer vilken tangent hjulet ska ha. Tryck en tangent, eller behåll R.
- Byt tangent senare under **Options > Controls > Command Wheel**, eller med knappen **Open key** i redigeringsskärmen.
- Håll inne tangenten, för musen över en ruta och släpp, eller klicka på rutan. Ett snabbt tryck lämnar hjulet öppet så att du kan klicka.
- **Edit wheel** i mitten: lägg till och ta bort kommandon och mappar, byt namn, flytta upp/ner. Max 8 rutor per nivå.
- Kommandot skrivs utan snedstreck, till exempel `home base`.

Inställningarna sparas i `config/commandwheel.json`. Där kan du också sätta
`"showKeyPromptOnEveryJoin": true` om du vill att tangentrutan visas vid varje join.

## Om bygget misslyckas
Modden är skriven mot Fabrics API för 26.2 men har inte kunnat provkompileras. Kopiera felmeddelandet och skicka det, så rättas det.
