import { createApp } from 'vue'

const app = createApp({
    template: `
    <div style="padding: 20px; font-family: Arial, sans-serif;">
      <h1>Web Lab Project</h1>
      <p>Frontend is working!</p>
      <button @click="testBackend">Test Backend Connection</button>
      <p>{{ message }}</p>
    </div>
  `,
    data() {
        return {
            message: ''
        }
    },
    methods: {
        async testBackend() {
            try {
                const response = await fetch('http://localhost:8080/web-lab/api/items')
                const data = await response.json()
                this.message = `Backend connected! Found ${data.length} items.`
            } catch (error) {
                this.message = `Error: ${error.message}`
            }
        }
    }
})

app.mount('#app')