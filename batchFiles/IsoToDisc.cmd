@echo off
REM =========================================================================
REM ====== SETTINGS =========================================================


    set batDir=%7%
	set gamesDir=%1
	set gameName=%2
	set ipAddy=%3
    set rarLoc=%4
	set isoName=%5
	set isoNameNoExt=%6
set extLoc=%8%
	
	
echo game name: %gameName%
REM echo rar location: %rarLoc%
REM echo batch dir: %batDir%
REM echo game dir: %gamesDir%
REM echo ipAddy: %ipAddy%
REM ====== LOG STUFF ========================================================	
for /f "tokens=2-4 delims=/ " %%a in ('date /T') do set year=%%c
for /f "tokens=2-4 delims=/ " %%a in ('date /T') do set month=%%a
for /f "tokens=2-4 delims=/ " %%a in ('date /T') do set day=%%b
set TODAY=%year%-%month%-%day%
for /f "tokens=1 delims=: " %%h in ('time /T') do set hour=%%h
for /f "tokens=2 delims=: " %%m in ('time /T') do set minutes=%%m
for /f "tokens=3 delims=: " %%a in ('time /T') do set ampm=%%a
set NOW=%hour%-%minutes%-%ampm%
	
	if exist %CD%\logs\Gamelog.%TODAY%%NOW%.txt del /q %CD%\logs\Gamelog.%TODAY%%NOW%.txt
	
	echo user variables set >> %batDir%\logs\Gamelog.%TODAY%%NOW%.txt




REM =========================================================================
REM Step 1: unrar
REM Step 2: delete rars (not yet coded as option)
REM Step 3: extract ISO to 360 via FTP
REM Step 4: Rename $SystemUpdate (cannot delete folder with files via ms ftp)
REM =========================================================================
echo =========================================================================
echo ======================== RarTo360 by Illusions0fGrander =================
echo =========================================================================
echo.


REM =========================================================================
REM ====== SET PATHS ========================================================

    set path="%batDir%";%path%
	echo paths set >> %batDir%\logs\Gamelog.%TODAY%%NOW%.txt

REM =========================================================================
REM ======== Start ISO Rip to FTP Location===================================

echo.
echo starting ISO rip to FTP %batDir%\logs\Gamelog.%TODAY%%NOW%.txt
echo.

cd /d %batDir%

echo Extracting %isoName% to %gamesDir%/%gameName% >> %batDir%\logs\Gamelog.%TODAY%%NOW%.txt
cd /d %rarLoc%

mkdir %extLoc%\%gameName%

exiso -d %extLoc%\%gameName% -s %rarLoc%\%isoName%

cd /d %batDir%


echo.
echo Process finished. >> %batDir%\logs\Gamelog.%TODAY%%NOW%.txt
REM if exist %batDir%\logs\%TODAY%%NOW%.txt rename %batDir%\logs\%TODAY%%NOW%.txt %batDir%\%TODAY%%NOW%%gameName%.txt
echo Batch completed. Please check the 360 for your game! 
echo.

