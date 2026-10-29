# Run your Employee Management System

## Start the website

Open PowerShell in `C:\employee system` and run:

```powershell
.\start-website.ps1
```

Keep that window open, then open **http://localhost:8080** in Chrome.

Administrator login:

- Username: **admin**
- Password: the password you chose during first startup (or your existing local password).

On a fresh installation, the start script prompts you to choose a password and saves it encrypted for your Windows account. The standalone frontend's `demo123` password does not apply to the database version.

If you supplied a custom `ADMIN_PASSWORD` on the initial startup, use that instead. Changing the environment later does not reset an existing administrator password.

## Employee records

The website at localhost uses real Java/JDBC/MySQL storage. Employees are retained when the website or database restarts. Opening `frontend/index.html` directly still uses separate browser demo data. Browser edits are not automatically imported into MySQL.

To add the 24 fictional sample employees through the Java API, run this in a second PowerShell window while the website is running:

```powershell
.\import-demo-employees.ps1
```

The import skips existing email addresses and does not replace existing records. Database IDs are allocated automatically and may differ from the standalone preview.

## Stop and restart

### Open on a phone over the same Wi-Fi

Stop the Java website with Ctrl+C if it is already running. Then start it with:

```powershell
.\start-phone-website.ps1
```

Connect your phone and laptop to the same trusted Wi-Fi. The script prints `http://YOUR_LAPTOP_WIFI_IP:8080`; open that address on your phone. On your laptop, **http://localhost:8080** also works. Phone mode listens on the specific Wi-Fi address and laptop loopback, rather than Ethernet or VPN interfaces. `localhost` on your phone points to the phone, not your laptop. Keep the laptop awake and the Java website running. Sign in with your administrator account.

If Windows blocks the connection, run `enable-phone-firewall.ps1` once from Administrator PowerShell in the project directory. It permits only this Java program on TCP port 8080 over Wi-Fi from the local subnet. MySQL stays bound to localhost. No router forwarding is needed. Guest Wi-Fi or hotspot client isolation can prevent device-to-device connections. The laptop IP may change when reconnecting.

To return to laptop-only access, stop Java and run `start-website.ps1` without `-PhoneAccess`. To remove the optional firewall rule, run `Remove-NetFirewallRule -Name 'PeopleDesk-Phone-WiFi-8080'` in Administrator PowerShell.

### Shut down

Press **Ctrl+C** in the website's PowerShell window to stop Java. To also shut down the database safely:

```powershell
.\stop-database.ps1
```

Run `start-website.ps1` again to restart both. Do not delete `.tools/mysql-data`; that folder contains the real database.

## Tools and local configuration

Java, Maven, and MySQL are stored inside `.tools`. MySQL runs only on `127.0.0.1:3307`; the website uses port 8080. The website defaults to laptop-only access; the phone start script enables network listening. No Windows service is required. Database passwords are generated randomly and stored as Windows-encrypted SecureString values in `.tools/database-settings.xml`. This file is tied to your Windows account; do not share the `.tools` folder or commit it.

There is no shared backend administrator password in the repository. Each fresh installation chooses its own password. Public hosting is not configured.

## Code and tests

Frontend: `frontend`. Backend: `backend/src/main/java/com/bhagyashree/peopledesk`. To run the automated tests:

```powershell
.\test-backend.ps1
```

Those tests use a temporary test database. A real MySQL verification is a separate check.

Verified on 29 October 2026: the Java API successfully created, read, updated, and deleted a disposable record in real MySQL. Records persisted after a safe database restart, with all 24 fictional sample employees retained. Run `verify-mysql.ps1` while the website is running to repeat that check; it briefly restarts this project's database.
