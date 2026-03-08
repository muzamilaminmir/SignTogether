const CACHE_NAME = 'signtogether-v1';
const urlsToCache = [
    '/',
    '/static/css/style.css',
    '/static/js/main.js',
    '/static/asset/two-fingers.png',
    '/static/asset/robot/idle.png',
    '/static/asset/robot/listening.png',
    '/static/asset/robot/thinking.png',
    '/static/asset/robot/signing.png',
    '/static/asset/robot/confused.png',
    '/static/asset/robot/emergency.png'
];

self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(urlsToCache))
    );
});

self.addEventListener('fetch', event => {
    event.respondWith(
        caches.match(event.request)
            .then(response => {
                if (response) {
                    return response;
                }
                return fetch(event.request);
            })
    );
});
