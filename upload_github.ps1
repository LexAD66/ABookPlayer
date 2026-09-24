<#
.SYNOPSIS
    Automatisches Git-Upload- & Veröffentlichungs-Skript für ABookPlayer auf GitHub (https://github.com/LexAD66/ABookPlayer).

.DESCRIPTION
    Führt Vorprüfungen durch, konfiguriert das Git-Remote 'origin', staged und committet Änderungen,
    erstellt auf Wunsch das Release-Tag (v2.0.0) und pusht den Code sowie Tags zu GitHub.
#>

param(
    [string]$RepoUrl = "https://github.com/LexAD66/ABookPlayer.git",
    [string]$Branch = "main",
    [string]$CommitMessage = "Release 2.0.0: Major stable release with Audible/iTunes metadata scraper, UI redesign & F-Droid readiness",
    [string]$Tag = "v2.0.0",
    [switch]$SkipTests,
    [switch]$AutoConfirm
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Text)
    Write-Host "`n[+] $Text" -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Text)
    Write-Host "[OK] $Text" -ForegroundColor Green
}

function Write-Warn {
    param([string]$Text)
    Write-Host "[!] $Text" -ForegroundColor Yellow
}

function Write-Err {
    param([string]$Text)
    Write-Host "[FEHLER] $Text" -ForegroundColor Red
}

Clear-Host
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "       ABook Player - GitHub Upload & Release Tool        " -ForegroundColor White -BackgroundColor DarkBlue
Write-Host "       Ziel-Profil: https://github.com/LexAD66            " -ForegroundColor Gray
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Arbeitsverzeichnis sicherstellen (Repo-Root)
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# 2. Prüfen, ob Git installiert ist
Write-Step "1/6: Überprüfe Git-Installation..."
try {
    $gitVer = (& git --version) 2>$null
    Write-Success "Git gefunden: $gitVer"
} catch {
    Write-Err "Git wurde im PATH nicht gefunden! Bitte installieren Sie Git for Windows."
    exit 1
}

# 3. Git Benutzerprüfung (Name & E-Mail)
$userName = (& git config user.name) 2>$null
$userEmail = (& git config user.email) 2>$null
if (-not $userName -or -not $userEmail) {
    Write-Warn "Git-Benutzerdaten sind noch nicht vollständig hinterlegt."
    if (-not $userName) {
        $userName = Read-Host "Bitte Git-Benutzernamen eingeben (z.B. LexAD66)"
        & git config user.name "$userName"
    }
    if (-not $userEmail) {
        $userEmail = Read-Host "Bitte Git-E-Mail eingeben"
        & git config user.email "$userEmail"
    }
}
Write-Success "Git-Autor: $userName <$userEmail>"

# 4. Optional: Unit-Tests ausführen
if (-not $SkipTests) {
    Write-Step "2/6: Gradle Unit-Tests überprüfen..."
    if (Test-Path "ABookPlayer\gradlew.bat") {
        if (-not $AutoConfirm) {
            $runTestChoice = Read-Host "Möchten Sie vor dem Upload die Unit-Tests ausführen? (J/n)"
            if ($runTestChoice -eq "" -or $runTestChoice -match "^[jJyY]") {
                Write-Host "Führe .\gradlew.bat test aus..." -ForegroundColor Gray
                Push-Location "ABookPlayer"
                try {
                    & ".\gradlew.bat" test --quiet
                    if ($LASTEXITCODE -eq 0) {
                        Write-Success "Alle 53 Unit-Tests erfolgreich bestanden!"
                    } else {
                        Write-Err "Tests sind fehlgeschlagen (Exit Code: $LASTEXITCODE). Upload abgebrochen."
                        Pop-Location
                        exit 1
                    }
                } finally {
                    Pop-Location
                }
            } else {
                Write-Warn "Tests übersprungen."
            }
        }
    }
} else {
    Write-Warn "Tests per Parameter -SkipTests übersprungen."
}

# 5. Remote 'origin' konfigurieren
Write-Step "3/6: Remote 'origin' überprüfen und konfigurieren..."
$existingRemote = (& git remote get-url origin) 2>$null

if ($existingRemote) {
    Write-Host "Aktuelles Remote 'origin': $existingRemote" -ForegroundColor Gray
    if ($existingRemote -ne $RepoUrl) {
        if (-not $AutoConfirm) {
            $changeRemote = Read-Host "Möchten Sie das Remote 'origin' auf '$RepoUrl' aktualisieren? (J/n)"
            if ($changeRemote -eq "" -or $changeRemote -match "^[jJyY]") {
                & git remote set-url origin $RepoUrl
                Write-Success "Remote 'origin' aktualisiert auf: $RepoUrl"
            }
        } else {
            & git remote set-url origin $RepoUrl
            Write-Success "Remote 'origin' aktualisiert auf: $RepoUrl"
        }
    } else {
        Write-Success "Remote 'origin' ist bereits korrekt konfiguriert."
    }
} else {
    Write-Host "Füge neues Remote 'origin' hinzu: $RepoUrl" -ForegroundColor Gray
    & git remote add origin $RepoUrl
    Write-Success "Remote 'origin' erfolgreich eingerichtet: $RepoUrl"
}

# 6. Branch vorbereiten (Standard 'main' auf GitHub)
Write-Step "4/6: Branch-Struktur prüfen..."
$currentBranch = (& git rev-parse --abbrev-ref HEAD) 2>$null
Write-Host "Aktueller Branch: $currentBranch" -ForegroundColor Gray

if ($currentBranch -ne $Branch) {
    Write-Host "Passe Haupt-Branch an GitHub-Standard an ('$currentBranch' -> '$Branch')..." -ForegroundColor Gray
    & git branch -M $Branch
    Write-Success "Aktiver Branch ist nun: $Branch"
}

# 7. Uncommitted Changes erkennen und committen
Write-Step "5/6: Dateistatus & Commits verwalten..."
$status = (& git status --short)
if ($status) {
    Write-Host "Folgende Änderungen/neue Dateien wurden gefunden:" -ForegroundColor Yellow
    & git status --short | Select-Object -First 15 | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
    $changedCount = ($status | Measure-Object).Count
    if ($changedCount -gt 15) {
        Write-Host "  ... und $($changedCount - 15) weitere Dateien." -ForegroundColor Gray
    }

    if (-not $AutoConfirm) {
        $customMsg = Read-Host "`nCommit-Nachricht (Enter für Standard: '$CommitMessage')"
        if ($customMsg.Trim() -ne "") {
            $CommitMessage = $customMsg.Trim()
        }
    }

    Write-Host "Staging aller Änderungen (git add .)..." -ForegroundColor Gray
    & git add .

    Write-Host "Erstelle Commit: '$CommitMessage'..." -ForegroundColor Gray
    & git commit -m "$CommitMessage"
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Commit erfolgreich erstellt."
    } else {
        Write-Warn "Keine neuen Änderungen zum Committen vorhanden oder Commit übersprungen."
    }
} else {
    Write-Success "Arbeitsverzeichnis ist sauber (Working tree clean). Keine uncommitteten Änderungen."
}

# Optional: Release-Tag erstellen
if ($Tag) {
    $existingTag = (& git tag -l $Tag)
    if (-not $existingTag) {
        Write-Host "Erstelle Release-Tag '$Tag'..." -ForegroundColor Gray
        & git tag -a "$Tag" -m "Release ${Tag}: ABook Player v2.0.0"
        Write-Success "Tag '$Tag' erfolgreich erstellt."
    } else {
        Write-Host "Tag '$Tag' existiert bereits lokal." -ForegroundColor Gray
    }
}

# 8. Zu GitHub hochladen (Push)
Write-Step "6/6: Upload zu GitHub durchführen..."
Write-Host "Ziel-Repository: $RepoUrl" -ForegroundColor Cyan
Write-Host "Branch:          $Branch" -ForegroundColor Cyan
Write-Host "Tags:            Inklusive aller Release-Tags" -ForegroundColor Cyan

if (-not $AutoConfirm) {
    $pushConfirm = Read-Host "`nJetzt zu GitHub pushen? (J/n)"
    if ($pushConfirm -ne "" -and $pushConfirm -notmatch "^[jJyY]") {
        Write-Warn "Push vom Benutzer abgebrochen."
        exit 0
    }
}

Write-Host "`nFühre 'git push -u origin $Branch --tags' aus..." -ForegroundColor Yellow
try {
    & git push -u origin $Branch --tags
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n==========================================================" -ForegroundColor Green
        Write-Host " 🎉 ERFOLG! ABook Player wurde zu GitHub hochgeladen!" -ForegroundColor Green
        Write-Host " Repository ansehen: https://github.com/LexAD66/ABookPlayer" -ForegroundColor White
        Write-Host "==========================================================" -ForegroundColor Green
    } else {
        throw "Git push beendete mit Fehlercode $LASTEXITCODE"
    }
} catch {
    Write-Err "Fehler beim Upload zu GitHub!"
    Write-Host "`nMögliche Ursachen & Lösungen:" -ForegroundColor Yellow
    Write-Host "1. Das Repository existiert noch nicht auf GitHub:" -ForegroundColor White
    Write-Host "   -> Bitte erstellen Sie auf https://github.com/new ein neues Repository mit dem Namen 'ABookPlayer' (öffentlich oder privat)." -ForegroundColor Gray
    Write-Host "2. Fehlende Zugriffsrechte / Authentifizierung:" -ForegroundColor White
    Write-Host "   -> GitHub verlangt einen Personal Access Token (PAT) anstelle Ihres Passworts." -ForegroundColor Gray
    Write-Host "   -> Token erstellen auf: https://github.com/settings/tokens (Scope: 'repo')." -ForegroundColor Gray
    Write-Host "   -> Alternativ: SSH-Key verwenden: 'git remote set-url origin git@github.com:LexAD66/ABookPlayer.git'" -ForegroundColor Gray
    Write-Host "3. Remote enthält bereits andere Commits:" -ForegroundColor White
    Write-Host "   -> Führen Sie 'git pull --rebase origin $Branch' aus und versuchen Sie es erneut." -ForegroundColor Gray
    exit 1
}
