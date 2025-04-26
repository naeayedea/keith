# Keith, Discord Bot

The fourth major version of keith, the largest change is rewriting the logic to use Spring with the Spring events
system. Maintains the same functionality of V3, with an (eventually) nicer code base to go with

[Add Me](https://discord.com/oauth2/authorize?client_id=624702573064224803&scope=botConfiguration&permissions=8)

## Updated Design

### Philosophy

I wanted KeithV4 to be a generic bot that can run on any platform (although it currently only exists on Discord), rather
than a discord specific bot. To accomplish this I needed to decouple my command/infrastructure implementations from
Discord and instead utilize strategy/transformer patterns to convert command information into formats understandable by
the platform that is being used

The basic idea is:

* Event is received from a source such as Discord, Teams, Slack, etc.
* Each platform has its own listener implementation able to understand the semantics of the platform
* Input is parsed, converted to an internal event
* Internal event is routed to the appropriate place such as a command handler
* Once an output is received from the handler, it is transformed into the appropriate output and delivered back to the
  source
* Each supported application will have its own module which can be run independently (from the module itself) or
  imported as a library to support multiple applications per process

The structure of the data will likely follow current message apps such as Discord, Slack etc. any other platforms could
be molded to fit this structure. For example, a basic WebSocket could be modelled as a server with a single channel.
