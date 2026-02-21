# Docker Installation Guide for Windows

## 🐳 Docker Desktop Installation

### Step 1: Download Docker Desktop
1. Go to: https://www.docker.com/products/docker-desktop/
2. Click "Download for Windows"
3. Save the installer to your Downloads folder

### Step 2: Install Docker Desktop
1. **Run the installer as Administrator**:
   - Right-click on `Docker Desktop Installer.exe`
   - Select "Run as administrator"

2. **Follow the installation wizard**:
   - Accept the license agreement
   - Choose "Use WSL 2 instead of Hyper-V" (recommended)
   - Click "OK" to install

3. **Restart your computer** when prompted

### Step 3: Start Docker Desktop
1. **Launch Docker Desktop**:
   - From Start Menu → Docker Desktop
   - Or double-click desktop shortcut

2. **Wait for Docker to start**:
   - Look for the Docker whale icon in system tray
   - Wait for it to turn green (this may take 2-3 minutes)
   - You may see a "Docker is starting" message

### Step 4: Verify Installation
Open PowerShell or Command Prompt and run:

```powershell
# Check Docker version
docker --version

# Check Docker Compose
docker-compose --version

# Test Docker with hello-world
docker run hello-world
```

If successful, you should see:
```
Hello from Docker!
This message shows that your installation is working correctly.
```

## 🔧 Alternative Installation Methods

### Method 2: Using Chocolatey (If you have Chocolatey)
```powershell
# Install Chocolatey (if not already installed)
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Install Docker Desktop
choco install docker-desktop

# Start Docker Desktop
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
```

### Method 3: Using Winget (Windows 10/11)
```powershell
# Install Docker Desktop using Winget
winget install Docker.DockerDesktop

# Start Docker Desktop
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
```

## ⚠️ Troubleshooting Common Issues

### Issue 1: "Docker is not running"
**Solution**:
1. Make sure Docker Desktop is running (green icon in system tray)
2. Restart Docker Desktop:
   - Right-click Docker icon → Restart
3. If still not working, restart your computer

### Issue 2: "WSL 2 installation is incomplete"
**Solution**:
1. Open PowerShell as Administrator
2. Run: `wsl --install`
3. Restart your computer
4. Start Docker Desktop again

### Issue 3: "Cannot connect to the Docker daemon"
**Solution**:
1. Open PowerShell as Administrator
2. Run: `net stop com.docker.service`
3. Run: `net start com.docker.service`
4. Restart Docker Desktop

### Issue 4: "Port already in use"
**Solution**:
1. Check what's using the port:
   ```powershell
   netstat -ano | findstr :3000  # For frontend
   netstat -ano | findstr :8082  # For intelligence service
   ```
2. Kill the process using the port:
   ```powershell
   taskkill /PID <PID> /F
   ```

## 🚀 Quick Verification Commands

After installation, run these commands to verify everything works:

```powershell
# Check Docker is running
docker info

# Check Docker Compose
docker-compose version

# Test with a simple container
docker run --rm -it alpine echo "Docker is working!"

# Check system resources
docker system df
```

## 📋 System Requirements

**Minimum Requirements**:
- Windows 10 64-bit: Pro, Enterprise, or Education (Build 19041 or higher)
- Windows 11 64-bit: Home, Pro, Enterprise, or Education
- At least 4GB RAM
- BIOS-level hardware virtualization support

**Recommended Requirements**:
- 8GB RAM or more
- SSD storage for better performance
- Modern CPU with virtualization support

## 🔄 Post-Installation Setup

1. **Configure Docker Desktop settings**:
   - Open Docker Desktop → Settings
   - Resources → Set memory limit (recommend 4GB+)
   - Resources → Set CPU limit (recommend 4+ cores)
   - Enable "Use WSL 2 based engine"

2. **Test with NTEWS project**:
   ```powershell
   cd C:\Users\opand\OneDrive\Desktop\projects\Ntews-mvp
   .\run_complete_pipeline.ps1
   ```

## 🎯 Next Steps

Once Docker is installed and running:

1. **Run the Windows PowerShell pipeline**:
   ```powershell
   cd C:\Users\opand\OneDrive\Desktop\projects\Ntews-mvp
   .\run_complete_pipeline.ps1
   ```

2. **Access your dashboard**:
   - Open browser: http://localhost:3000
   - Wait for all services to start (2-3 minutes)

3. **Verify services**:
   ```powershell
   docker ps  # See running containers
   docker-compose logs  # View logs
   ```

---

## 🆘 Need Help?

If you encounter issues:

1. **Check Docker Desktop logs**:
   - Docker Desktop → Troubleshoot → Logs

2. **Reset Docker Desktop**:
   - Docker Desktop → Troubleshoot → Reset to factory defaults

3. **Consult Docker documentation**:
   - https://docs.docker.com/desktop/windows/install/

4. **Common Windows issues**:
   - Make sure WSL 2 is enabled
   - Check Windows Firewall settings
   - Verify virtualization is enabled in BIOS

---

**🎉 After Docker installation, your NTEWS MVP will be ready to run with full containerized services!**
