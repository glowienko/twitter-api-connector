exec docker build -t patrykglow/twitter-connector .
exec docker run -p 8080:8080 \
-e TWITTER_CONSUMER_KEY=RLSrphihyR4G2UxvA0XBkLAdl \
-e TWITTER_CONSUMER_SECRET=FTz2KcP1y3pcLw0XXMX5Jy3GTobqUweITIFy4QefullmpPnKm4 \
-e TWITTER_ACCESS_TOKEN=access-token \
-e TWITTER_ACCESS_TOKEN_SECRET=token-secret \
patrykglow/twitter-connector

