package net.fangstar.wifi.portal.controller.module;

public class UserLoginModule {
        public String deviceCode;
        private String deviceName;
        private String deviceType;
        private String deviceOs;
        private String flag;
        private String password;
        private String username;

        public String getDeviceCode() {
            return deviceCode;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }

        public void setDeviceCode(String deviceCode) {
            this.deviceCode = deviceCode;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getDeviceOs() {
            return deviceOs;
        }

        public void setDeviceOs(String deviceOs) {
            this.deviceOs = deviceOs;
        }

        public String getFlag() {
            return flag;
        }

        public void setFlag(String flag) {
            this.flag = flag;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
}
