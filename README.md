# Simple Trading Bot

## How to launch

First of all build the project with `gradlew build` or open in IDE

There are several launchers in this repo.

`SingleBotStarter` is interactive single bot launcher; you need to input `productId`, `buyPrice`, `lower` and `upper` 

I didn't care about dependency injection just because it's a simple app. So some instances like service and object mapper are created right away whenever they are needed.

