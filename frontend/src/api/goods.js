import request from '@/utils/request';

// 根据手机号获取商品列表
export function getGoodsByPhone(phone) {
  return request({
    url: '/api/getGoodsByPhone',
    method: 'get',
    params: { phone }
  });
}

// 生成AI广告文案
export function generateAIAd(params) {
  return request({
    url: '/api/aiAD',
    method: 'post',
    data: params
  });
} 