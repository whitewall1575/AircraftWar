# AircraftWar

## Experiment 3 (Strategy Pattern) Status

Implemented features aligned with the Experiment 3 checklist:

- Strategy-based shooting system (`ShootStrategy`) with three concrete strategies:
  - `StraightShootStrategy`
  - `ScatterShootStrategy`
  - `CircleShootStrategy`
- Aircraft shooting decoupled from aircraft classes and delegated by strategy context (`AbstractAircraft`).
- Hero firepower props:
  - `FireProp` switches hero shooting to scatter.
  - `SuperFireProp` switches hero shooting to circle.
- Boss behavior:
  - Boss spawns when score reaches threshold (500, 1000, ...), if no active boss exists.
  - Boss hovers at top and moves horizontally only.
  - Boss uses 20-bullet circle shooting each attack cycle.
  - Boss drops 3 random props when destroyed.
- `BombProp` and `FreezeProp` print activation messages in console.

## UML

- Added strategy UML: `uml/Strategy.puml`

## Quick Verify (compiled)

```powershell
Set-Location 'D:\Java\JavaProject\Software Construction\实验一\AircraftWar-base'
$files = Get-ChildItem '.\src' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
& javac -encoding UTF-8 -cp '.;lib/*;src' $files
```
