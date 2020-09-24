# keith
discordbot
Second version of keith bot, major quality of life improvements and complete overhaul of the source code.Prefixes are now unique to each server and there is 
now the capability to ban users or servers and also give users more powers via a command.

Commands have also been made abstract, simplifying the messagehandler code. Commands are now threaded which allows the bot to continue even 
if a users actions are taking a significant amount of time, threads are automatically killed after 10 seconds.

More general commands need to be added as well as the bots ability to process images, 
this will come in a later version. v2 is a strong foundation for future work.
