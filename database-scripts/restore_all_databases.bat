@echo off
setlocal enabledelayedexpansion
set PGPASSWORD=password
set PGUSER=xparadize
set HOST=localhost

:: Список баз данных и соответствующих файлов
set "databases=SecurityServer PaymentMicroservice ProductMicroservice NotificationMicroservice CompanyMicroservice"
set "backup_files=SecurityServer.sql PaymentMicroservice.sql ProductMicroservice.sql NotificationMicroservice.sql CompanyMicroservice.sql"

:: Преобразуем в массивы
set i=0
for %%d in (%databases%) do (
    set /a i+=1
    set db[!i!]=%%d
)

set i=0
for %%f in (%backup_files%) do (
    set /a i+=1
    set backup[!i!]=%%f
)

:: Восстанавливаем базы данных
set j=1
for %%j in (%databases%) do (
    echo Восстанавливаю базу данных !db[%j%]! из файла !backup[%j%]!
    pg_restore -U %PGUSER% -h %host% -d !db[%j%]! "!backup[%j%]!"

    if !ERRORLEVEL! EQU 0 (
        echo Восстановление базы данных !db[%j%]! завершено успешно.
    ) else (
        echo Ошибка при восстановлении базы данных !db[%j%]!.
    )
)

set PGPASSWORD=
set PGUSER=
set HOST=
pause
