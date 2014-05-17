package server;

import java.net.Socket;

public interface FTPScenario {

	public void ConnectionEstablishment(Socket client, FTPCommand command);
	public void Login(Socket client, FTPCommand command);
	public void Logout(Socket client, FTPCommand command);
	public void TransferParameters(Socket client, FTPCommand command);
	public void FileActionCommands(Socket client, FTPCommand command);
	public void InformationalCommands(Socket client, FTPCommand command);
	public void MiscellaneousCommands(Socket client, FTPCommand command);

	// Logout, Transfer parameters, File action commands,  Informational commands, Miscellaneous commands 
	/*

            Connection Establishment
               120
                  220
               220
               421
            Login
               USER
                  230
                  530
                  500, 501, 421
                  331, 332
               PASS
                  230
                  202
                  530
                  500, 501, 503, 421
                  332
               ACCT
                  230
                  202
                  530
                  500, 501, 503, 421
               CWD
                  250
                  500, 501, 502, 421, 530, 550
               CDUP
                  200
                  500, 501, 502, 421, 530, 550
               SMNT
                  202, 250
                  500, 501, 502, 421, 530, 550
            Logout
               REIN
                  120
                     220
                  220
                  421
                  500, 502
               QUIT
                  221
                  500




Postel & Reynolds                                              [Page 50]


                                                                        
RFC 959                                                     October 1985
File Transfer Protocol


            Transfer parameters
               PORT
                  200
                  500, 501, 421, 530
               PASV
                  227
                  500, 501, 502, 421, 530
               MODE
                  200
                  500, 501, 504, 421, 530
               TYPE
                  200
                  500, 501, 504, 421, 530
               STRU
                  200
                  500, 501, 504, 421, 530
            File action commands
               ALLO
                  200
                  202
                  500, 501, 504, 421, 530
               REST
                  500, 501, 502, 421, 530
                  350
               STOR
                  125, 150
                     (110)
                     226, 250
                     425, 426, 451, 551, 552
                  532, 450, 452, 553
                  500, 501, 421, 530
               STOU
                  125, 150
                     (110)
                     226, 250
                     425, 426, 451, 551, 552
                  532, 450, 452, 553
                  500, 501, 421, 530
               RETR
                  125, 150
                     (110)
                     226, 250
                     425, 426, 451
                  450, 550
                  500, 501, 421, 530




Postel & Reynolds                                              [Page 51]


                                                                        
RFC 959                                                     October 1985
File Transfer Protocol


               LIST
                  125, 150
                     226, 250
                     425, 426, 451
                  450
                  500, 501, 502, 421, 530
               NLST
                  125, 150
                     226, 250
                     425, 426, 451
                  450
                  500, 501, 502, 421, 530
               APPE
                  125, 150
                     (110)
                     226, 250
                     425, 426, 451, 551, 552
                  532, 450, 550, 452, 553
                  500, 501, 502, 421, 530
               RNFR
                  450, 550
                  500, 501, 502, 421, 530
                  350
               RNTO
                  250
                  532, 553
                  500, 501, 502, 503, 421, 530
               DELE
                  250
                  450, 550
                  500, 501, 502, 421, 530
               RMD
                  250
                  500, 501, 502, 421, 530, 550
               MKD
                  257
                  500, 501, 502, 421, 530, 550
               PWD
                  257
                  500, 501, 502, 421, 550
               ABOR
                  225, 226
                  500, 501, 502, 421






Postel & Reynolds                                              [Page 52]


                                                                        
RFC 959                                                     October 1985
File Transfer Protocol


            Informational commands
               SYST
                  215
                  500, 501, 502, 421
               STAT
                  211, 212, 213
                  450
                  500, 501, 502, 421, 530
               HELP
                  211, 214
                  500, 501, 502, 421
            Miscellaneous commands
               SITE
                  200
                  202
                  500, 501, 530
               NOOP
                  200
                  500 421




	 */
}