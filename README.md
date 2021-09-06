# Keith, Discord Bot

Third major version of keith, the bot has been rebuilt from the ground up. V3 has the same functionality as V2 and more
but with significantly tidier codebase, greatly improved database and user/server manager code. A list of improvements
and additions can be found below: 

## Backend Changes

### Command Interface
    The regular command interface operates almost identically with the exception of hidden commands and 
    the brand new channel command interface which allows commands to be linked to specific channels over
    the course of their execution. Due to various changes the code of the handler is more complex but is 
    more powerful than v2 as channel commands are very easily implemented using the ChannelCommandManager.

### Resource Usage
    Steps have been taken to reduce overall resource usage such as repeated calls to the database to 
    retrieve the same information, the bot now caches server/user objects for a short period of time
    so that if the same user/server is required over a short period the database is not unnecessarily
    loaded.

### Database
    The database code is now vastly superior to v2's iteration. Coupling between the database and other
    classes has been completely removed and the database code functions standalone. The  database code 
    can prepare statements and executethem with some different methods depending on the desired format 
    of the results. The strings for preparing statements are passed externally into the code which 
    significantly reduces coupling of the code. 

    The database has also been migrated to mysql over sqllite which should greatly improve performance 
    with multiple write queries at once. The security has also been improved as all queries use prepared 
    statements with the exception of admin use so that sql injection attacks are extremely unlikely. 

### User/Server Managers
    User/Server Manager code has been greatly improved with the Server/User objects becoming internal
    classes rather than standalone. As discussed above, the managers now store an internal cache of users
    so that repeated loading of the same users/servers wont require repeated database queries in a short 
    period of time. Methods relating to users or servers are now kept within their respective classes
    rather than the manager themselves getting the information.

### Assorted Changes
    Much of the utility code has now been made static to reduce the need for a utilities class every
    single time a util was needed, a serious quality of life improvement. The managers have also been 
    implemented as singletons to ensure that the same object is accessed no matter where they are used
    - very important change. 

## User Facing Changes

### Command Aliases
    commands can now easily be aliases thanks to the new MultiMap class, 

### New Commands
    echo: bot will repeat anything passed to it via the command - only for users with higher access level
    
### Improved Commands
    guess: the number guess game now utilises the brand new channel command interface which means that 
    users no longer have to type ?guess [guess] to have the bot evaluate their input. Now users can 
    simply type their guess and keith will automatically evaluate it and respond with a success message
    or a reaction for higher/lower

    pin: the pin command has been recreated with greater utility such as typing ?pin [text] will pin the
    text without requiring the message to be sent first, also the originating channel with be noted so 
    that users do not accidentally go to a channel they do not want to with the hyperlink. 

    the major improvement with pin is that it will automatically create a pin channel if one does not 
    exist, previously the database would need to be updated by the bot owner personally which was less
    than ideal, the bot requires manage channel permission for this which will be communicated to users
    in the event that it does not. 

### Improved Prefixes
    prefixes can now be longer than one character due to security improvements in the database, due to 
    the way user input is parsed, prefixes cannot themselves contain spaces but the single word can be
    as long as discord will allow - this is subject to change and an artificial limit me be included to
    keep prefixes practical such as 10-20 characters. 

### Assorted Changes
    the bot now sends a typing signal to discord when responding to commands so that the user knows their
    command is being processed if it happens to take longer than usual. 

    users can now be rate limited from using the bot - separate from discord rate limiting - if their use 
    exceeds a given value in a short time.

    
