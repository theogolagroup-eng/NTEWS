/** @type {import('next').NextConfig} */
const nextConfig = {
  transpilePackages: ['antd', '@ant-design/icons'],
  async rewrites() {
    return [
      {
        source: '/api/intelligence/:path*',
        destination: 'http://localhost:8082/api/intelligence/:path*',
      },
      {
        source: '/api/alerts/:path*',
        destination: 'http://localhost:8084/api/alerts/:path*',
      },
      {
        source: '/api/predictions/:path*',
        destination: 'http://localhost:8083/api/predictions/:path*',
      },
    ];
  },
  webpack: (config) => {
    config.resolve.fallback = {
      ...config.resolve.fallback,
      fs: false,
      net: false,
      tls: false,
    };
    return config;
  },
};

module.exports = nextConfig;
