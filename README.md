# EnhancedRAFT

>host.Host

The host.Host would start a enhanced RAFT server on the 
current machine. A state log file, storing the committed
 value in the current host, and a log file, storing the 
 information about the vote during the election period. 
Without further instruction, the host would remain isolated,
and it is running without RAFT functionality.

>host.Clent

The host.Client acts as a interface. It is used to connect
all the server together.  All the commands are sent from here. 
The following are the command a user can use:

    changevalue <state name: String\> <state value: Int\> <\?byzantine command: Bool\>
    
>>This instruction would send the new value with the new 
state name to the leader. The first parameter is key word
"changevalue"; the second parameter is state name, which
is a string without empty space; the third parameter is 
new state value, a integer; the last parameter is an 
optional Bool value, true stands for the leader would 
make Byzantine move on this command.


    add (<host ip: String>, <host port: String>) ...
    
>> This instruction would connect all the host into one 
cluster and start the remaining functionality of the RAFT. 


    byzantineenable / byzantinedisable
      
>>These two instruction would activate/deactivate the 
enhanced Byzantine fault tolerance functionality in all 
host machines. 

    help
    
>>Cheat sheet of all the instructions
