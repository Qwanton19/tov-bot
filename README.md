# TOV Bot

Minecraft Fabric mod for Monumenta adapted from [Ninjabrain Bot](https://github.com/Ninjabrain1/Ninjabrain-Bot) for TOV and SKR locations. Uses Bayesian triangulation to calculate which location you're looking at, and recommends where to go next for higher certainty

## Usage

Press F3+C while looking toward a location, then run:
```
/tov
```
or
```
/skr
```

For SKR, also returns coordinates if a riddle appeared in chat within the last minute. Works across multiple throws (within 15 minutes) for better probabilities. Returns probability-ranked locations with city recommendations for your next throw

To clear throws for new search:
```
/tovreset
```
```
/skrreset
```


## How It Works

Reads F3+C clipboard data (player position + look direction) and uses Gaussian probability distribution to calculate likelihood of each possible location. Multi-throw triangulation combines measurements from different positions using Bayesian conditioning so that each throw narrows down the probability distribution of possible locations. City recommendations tell you where to go next for maximum certainty gain

Throws are tracked separately for TOV and SKR, reset on server disconnect or after 10 minutes. No auto-reset on angle difference since triangulation requires different viewing angles (first throw north, second throw east = better accuracy)

## Download

Get the latest release for Minecraft 1.20.4 Fabric from the [Releases page](https://github.com/Qwanton19/tov-bot/releases)
