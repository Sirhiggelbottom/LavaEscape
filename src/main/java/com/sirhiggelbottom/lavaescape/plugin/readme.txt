 Command hierarchy:
 args length
 ->   1       2         3          4          5            6           7
            args[0]   args[1]    args[2]    args[3]      args[4]     args[5]
   /Lava -> arena  -> arenaName-> start  -> arenaName
                               -> stop   -> arenaName
                               -> create -> arenaName
                               -> delete -> arenaName
                               -> config -> arenaName -> minplayers -> int
                                                      -> maxplayers -> int
                                                      -> mode       -> competitive
                                                                    -> server
                                                      -> set-area   -> arena
                                                                    -> lobby
         -> reload -> arenaName
         -> list
         -> help
         -> lwand
         -> join   -> arenaName
         -> leave  -> arenaName
