# Simple Trading Bot

## How to launch

First of all build the project with `gradlew build` or open in IDE

There are several launchers in this repo:

`SingleBotStarter` is interactive single bot launcher; you need to input `productId`, `buyPrice`, `lower` and `upper`; all optional, can just press enter and skip all

`LimitedBalanceBotStarter` simulates the user with some random wallet balance; it will start the bot for fixed productId and run it until wallet balance is > 0

`MultipleBotsStarter` starts multiple bots for each product in parallel, so the user has multiple positions opened at the same time; there is 1 web socket connection though with many `onMessageListeners`; program stops after several cycles

## About the app

I've set some logging levels to INFO just for demonstration purposes - so that the person who will launch the app (you :)) can see what's happening. Some of those loggers I would normally set to DEBUG.

I haven't implemented all validations, for ex. the bot will accept `null` productId but will do nothing, probably in this case bot shouldn't be allowed to start.

I didn't care about dependency injection just because it's a simple app. So instances of `services` and `ObjectMapper` are created right away whenever they are needed.

I haven't wrote many tests because I had not much time; obviously all the bot code should be tested with different inputs, with different trading quote messages incoming speed

I can see a lot of things to enhance, for example to make bot highly configurable, add extensive logging and hooks (listeners) for different events happening in the bot

also to add interface layers so tht bot can auto-subscribe itself (check subscription) or retry on failed open / close position requests (and signal such cases to listeners)4

would be nice to make bot thread-safe, `start` method for example, but I expect that it would be used as single-instance per job (thread)... but would be nice to be sure :)

also a bot may need a smarter cleanup i.e. close position on stopping for example


...

Thanks for reading this :)

## Used 3rd party

- javax.websocket api + tyrus reference impl
- okhttp
- jackson
- slf4j bridge + logback
- typesafe config
- junit