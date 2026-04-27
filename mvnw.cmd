@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@echo off
setlocal

set ERROR_CODE=0

:init
@REM Decide how to startup depending on the version of windows

@REM -- Windows NT with Novell LoginScript (Novell may load a dynamic library
@REM -- that resets the PATHEXT variable from C:\ninit.exe)  -- jharti@novell.com
if "%OS%"=="Windows_NT" goto WinNT

@REM -- Win98 ME
if NOT "%JAVA_HOME%"=="" goto java_home_win98
echo.
echo ERROR: JAVA_HOME not found in your environment, please set it
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto end
:java_home_win98
if exist "%JAVA_HOME%\bin\java.exe" goto java_home_ok
if exist "%JAVA_HOME%\bin\java.cmd" goto java_home_ok
echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = "%JAVA_HOME%"
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto end

@REM -- Windows NT
:WinNT
if "%JAVA_HOME%"=="" goto no_java_home

if exist "%JAVA_HOME%\bin\java.exe" goto java_home_ok
if exist "%JAVA_HOME%\bin\java.cmd" goto java_home_ok

echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = "%JAVA_HOME%"
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto end

:no_java_home
echo.
echo ERROR: JAVA_HOME not found in your environment, please set it
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto end

:java_home_ok
if exist "%JAVA_HOME%\bin\jps.exe" goto init_maven
echo.
echo ERROR: JAVA_HOME is not pointing to a JDK.
echo JAVA_HOME = "%JAVA_HOME%"
echo Please update your environment so that it points to at least a JDK.
echo.
goto end

:init_maven
@REM Find mvn.cmd
if exist "%~dp0mvn.cmd" goto run

echo %0 is not properly configured
goto end

:run
"%JAVA_HOME%\bin\java.exe" %MAVEN_OPTS% -classpath %CLASSPATH% org.apache.maven.wrapper.MavenWrapperMain %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
exit /b %ERROR_CODE%
