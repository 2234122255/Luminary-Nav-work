<template>
  <div class="login-view">
    <section class="login-panel">
      <div class="login-heading">
        <h1>用户登录</h1>
      </div>

      <div class="login-tabs">
        <button :class="{ active: activeTab === 'user' || activeTab === 'register' }" @click="setActiveTab('user')">用户</button>
        <button :class="{ active: activeTab === 'admin' }" @click="setActiveTab('admin')">管理员</button>
      </div>

      <form v-if="activeTab === 'user'" class="login-form" @submit.prevent="loginAsUser">
        <label class="field">
          <span>账号</span>
          <input v-model.trim="userForm.username" type="text" placeholder="请输入账号">
        </label>

        <label class="field">
          <span>密码</span>
          <input v-model="userForm.password" type="password" placeholder="请输入密码">
        </label>

        <button class="register-link" type="button" @click="setActiveTab('register')">注册</button>

        <p v-if="message" class="form-message">{{ message }}</p>
        <button class="submit-btn" type="submit">登录</button>
      </form>

      <form v-else-if="activeTab === 'register'" class="login-form" @submit.prevent="registerUser">
        <label class="field">
          <span>账号</span>
          <input v-model.trim="registerForm.username" type="text" placeholder="请输入账号">
        </label>

        <label class="field">
          <span>密码</span>
          <input v-model="registerForm.password" type="password" placeholder="请输入密码">
        </label>

        <label class="field">
          <span>确认密码</span>
          <input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码">
        </label>

        <p v-if="message" class="form-message">{{ message }}</p>
        <button class="submit-btn" type="submit">注册</button>
      </form>

      <form v-else class="login-form" @submit.prevent="loginAsAdmin">
        <label class="field">
          <span>管理员账号</span>
          <input v-model.trim="adminForm.username" type="text" placeholder="administrator">
        </label>

        <label class="field">
          <span>管理员密码</span>
          <input v-model="adminForm.password" type="password" placeholder="请输入密码">
        </label>

        <p v-if="message" class="form-message">{{ message }}</p>
        <button class="submit-btn" type="submit">进入管理员页面</button>
      </form>
    </section>
  </div>
</template>

<script>
const ADMIN_USERNAME = 'administrator'
const ADMIN_PASSWORD = '123456' // 修改管理员密码的位置

export default {
  name: 'LoginView',
  data() {
    return {
      activeTab: 'user',
      message: '',
      userForm: {
        username: '',
        password: ''
      },
      registerForm: {
        username: '',
        password: '',
        confirmPassword: ''
      },
      adminForm: {
        username: ADMIN_USERNAME,
        password: ''
      }
    }
  },
  methods: {
    saveAuth(auth) {
      localStorage.setItem('luminaryAuth', JSON.stringify(auth))
      window.dispatchEvent(new Event('luminary-auth-change'))
    },
    setActiveTab(tab) {
      this.activeTab = tab
      this.message = ''
    },
    getUsers() {
      try {
        const users = JSON.parse(localStorage.getItem('luminaryUsers') || '[]')
        return Array.isArray(users) ? users : []
      } catch (error) {
        return []
      }
    },
    saveUsers(users) {
      localStorage.setItem('luminaryUsers', JSON.stringify(users))
    },
    loginAsUser() {
      this.message = ''
      if (!this.userForm.username || !this.userForm.password) {
        this.message = '请填写账号和密码。'
        return
      }

      const user = this.getUsers().find(item => item.username === this.userForm.username)
      if (!user || user.password !== this.userForm.password) {
        this.message = '账号或密码不正确，请先注册或重新输入。'
        return
      }

      this.saveAuth({
        role: 'user',
        username: user.username,
        nickname: user.nickname || '',
        gender: user.gender || '',
        avatar: user.avatar || ''
      })
      this.$router.push('/')
    },
    registerUser() {
      this.message = ''
      if (!this.registerForm.username || !this.registerForm.password || !this.registerForm.confirmPassword) {
        this.message = '请填写账号、密码和确认密码。'
        return
      }
      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        this.message = '两次输入的密码不一致。'
        return
      }
      if (this.registerForm.username === ADMIN_USERNAME) {
        this.message = '该账号为管理员账号，请更换普通用户账号。'
        return
      }

      const users = this.getUsers()
      if (users.some(user => user.username === this.registerForm.username)) {
        this.message = '账号已存在，请更换账号。'
        return
      }

      users.push({
        username: this.registerForm.username,
        password: this.registerForm.password,
        nickname: '',
        gender: '',
        avatar: ''
      })
      this.saveUsers(users)
      this.$router.push('/')
    },
    loginAsAdmin() {
      this.message = ''
      if (this.adminForm.username !== ADMIN_USERNAME || this.adminForm.password !== ADMIN_PASSWORD) {
        this.message = '管理员账号或密码不正确。'
        return
      }

      this.saveAuth({
        role: 'admin',
        nickname: '管理员',
        username: ADMIN_USERNAME,
        avatar: ''
      })
      this.$router.push('/admin-gallery')
    }
  }
}
</script>

<style scoped>
.login-view {
  min-height: calc(100vh - 220px);
  padding: 120px 24px 40px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.login-panel {
  width: min(520px, 100%);
  padding: 34px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(100, 150, 255, 0.28);
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.28);
  backdrop-filter: blur(18px);
}

.login-heading {
  text-align: center;
  margin-bottom: 26px;
}

.login-heading h1 {
  margin: 0;
  color: #fff;
  font-size: 30px;
  font-weight: 700;
}

.login-tabs {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 24px;
}

.login-tabs button {
  padding: 12px 16px;
  border: 1px solid rgba(100, 150, 255, 0.25);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.72);
  cursor: pointer;
  transition: all 0.25s ease;
}

.login-tabs button.active,
.login-tabs button:hover {
  color: #fff;
  border-color: rgba(102, 126, 234, 0.8);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.42), rgba(118, 75, 162, 0.38));
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: rgba(255, 255, 255, 0.86);
  font-size: 14px;
}

.field input,
.field select {
  width: 100%;
  min-height: 46px;
  padding: 0 14px;
  border-radius: 8px;
  border: 1px solid rgba(100, 150, 255, 0.28);
  background: rgba(9, 12, 30, 0.62);
  color: #fff;
  outline: none;
  transition: all 0.25s ease;
}

.field input:focus,
.field select:focus {
  border-color: #667eea;
  box-shadow: 0 0 18px rgba(102, 126, 234, 0.26);
}

.field input::placeholder {
  color: rgba(255, 255, 255, 0.42);
}

.register-link {
  align-self: flex-end;
  margin-top: -10px;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.82);
  font-size: 14px;
  cursor: pointer;
  padding: 0;
}

.register-link:hover {
  color: #fff;
}

.form-message {
  margin: 0;
  color: #fca5a5;
  font-size: 14px;
  text-align: center;
}

.submit-btn {
  min-height: 48px;
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  background: linear-gradient(135deg, #667eea, #764ba2);
  box-shadow: 0 12px 28px rgba(102, 126, 234, 0.3);
  transition: all 0.25s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 34px rgba(102, 126, 234, 0.4);
}

@media (max-width: 640px) {
  .login-view {
    padding-top: 180px;
  }

  .login-panel {
    padding: 24px;
  }
}
</style>
