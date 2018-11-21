
const routes = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: '', component: () => import('pages/Index.vue') }
    ]
  },
  {
    path: '/authorize',
    component: () => import('layouts/XmasLayout.vue'),
    children: [
      { path: '', component: () => import('pages/Authorize.vue') }
    ]
  },
  {
    path: '/:token',
    component: () => import('layouts/XmasLayout.vue'),
    children: [
      { path: '', component: () => import('pages/XmasRegistry.vue') }
    ]
  }
]

// Always leave this as last one
if (process.env.MODE !== 'ssr') {
  routes.push({
    path: '*',
    component: () => import('pages/Error404.vue')
  })
}

export default routes
