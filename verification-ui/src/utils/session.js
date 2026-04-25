import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'

export function useSessionActions(router) {
  const authStore = useAuthStore()

  const logout = async () => {
    await ElMessageBox.confirm('确定退出当前登录会话吗？', '退出登录', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning',
    })
    authStore.logout()
    router.push('/login')
  }

  return { logout }
}
