/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package OccurrencesSearch;

import Occurrences.OccurrencesList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

/**
 * Class that manages an authentication log given and extracts IPs and dates that
 * they tried to connect by SSH ineffectively
 * @author Aaron Rodriguez Bueno
 */
public class OccurrencesSearch {
    private String file;
    
    /**
     * Default constructor
     */
    public OccurrencesSearch(){
        file = new String();
    }
    
    /**
     * Constructor
     * @param filename The name of the file to manage
     */
    public OccurrencesSearch(String filename){
        file = filename;
    }
    
    /**
     * To get the file name
     * @return The file name
     */
    public String getFilename(){
        return file;
    }
    
    /**
     * To set the file name
     * @param filename The file name to set
     */
    public void  setFilename(String filename){
        file = filename;
    }
    
    /**
     * Searchs the IPs in a authentication log file that they tried to connect by SSH 
     * and extracts too the number of trys and the date and time of the first try 
     * @param number The number minimum that they tried to connect 
     * @param seconds The seconds before now to start to count trys
     * @return The list with the IPs that passes the number of trys and the time
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public OccurrencesList searchOccurrences(int number, int seconds, String os) throws FileNotFoundException, IOException{
        OccurrencesList occurrences = new OccurrencesList();
        String ip="";
        
        if(file != null){
            //Opening the file
           
                /*String line, ip, format = "yyyy MMM dd HH:mm:ss";
                Date date, current_date, last_cycle;
                Calendar calendar;
                String [] parts;
                
                
                current_date = Calendar.getInstance().getTime();                
                calendar = Calendar.getInstance();
                calendar.setTime(current_date);

                //Then we take the current year
                String year = Integer.toString(calendar.get(Calendar.YEAR));
                
                //For the first cycle we need last_cycle
                last_cycle = new Date(current_date.getTime()-seconds*1000); */
                    
                //Looking for valid IPs at the time given

                //FOR LINUX OS

                if(os.contains("linux")){
                    occurrences = searchOccurrencesLinux(number,seconds);

                }else if(os.contains("windows")){ //FOR WINDOWS OS
                    occurrences = searchOccurrencesWindows(number,seconds);


                }else{ //NO VALID OS
                    ip = "";
                    System.out.println(ip+"NO VALID IP ELSE TOTAL");
                    System.out.println("ERROR TAKING IP (no matches found)");
                }
                                          
        }
                    
        
        //Return the list with the IPs with the equal or greater number of occurrences
        return occurrences.occurrencesWithNumberAboveOrEqual(number);
    }
    
    
    public OccurrencesList searchOccurrencesLinux(int number, int seconds) throws FileNotFoundException, IOException{
        OccurrencesList occurrences = new OccurrencesList();
        
        if(file != null){
            //Opening the file
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line, ip, format = "yyyy MMM dd HH:mm:ss";
                Date date, current_date, last_cycle;
                Calendar calendar;
                String [] parts;
                
                
                current_date = Calendar.getInstance().getTime();                
                calendar = Calendar.getInstance();
                calendar.setTime(current_date);

                //Then we take the current year
                String year = Integer.toString(calendar.get(Calendar.YEAR));
                
                //For the first cycle we need last_cycle
                last_cycle = new Date(current_date.getTime()-seconds*1000); 
                    
                //Looking for valid IPs at the time given
                while ((line = br.readLine()) != null) {
                    
                    //System.out.println(line);
             
                    if(line.contains("sshd") || //An ssh occurrence
                            line.contains("port 22")){    
                        if(line.contains("refused connect") ||      //Refused connect
                                line.contains("Invalid user") ||    //Invalid user
                                line.contains("Failed password")){     //Valid user but invalid password (3 times)
                            //Checking date and time
                            parts = line.split(" ");

                            //FOR LINUX OS
                            //System.out.println(os);
                                
                            //First we take the month, day and time
                            String month = parts[0];
                            String day, time;
                            if(StringUtils.isNumeric(parts[1])){
                                day = parts[1];
                                time = parts[2];
                            }
                            else{   //Sometimes we have to get pos 2
                                day = parts[2];
                                time = parts[3];
                            }

                            //Now we change the string into a valid date
                            switch(month){
                                case "Jan":
                                    month = "Ene";
                                    break;
                                case "Apr":
                                    month = "Abr";
                                    break;
                                case "Aug":
                                    month = "Oct";
                                    break;
                                case "Dec":
                                    month = "Dic";
                                    break;
                            }

                            date = new SimpleDateFormat(format).parse(year+" "+month+" "+day+" "+time);

                            //If the date with the current year is after the current date 
                            //(when the current year changed), we take the last year before this one
                            if(date.after(current_date)){
                                year = Integer.toString(calendar.get(Calendar.YEAR)-1);
                                date = new SimpleDateFormat(format).parse(year+" "+month+" "+day+" "+time);
                            }

                            //Finally, we compare if the new date and time are between this cycle
                            //and the last one

                            if(date.after(last_cycle) && date.before(current_date)){
                                //Taking the IP
                                if(line.contains("refused connect")){
                                    ip = line.substring(line.indexOf("(") + 1);
                                    ip = ip.substring(0, ip.indexOf(")"));
                                }
                                else if(line.contains("Invalid user")){
                                    ip = parts[parts.length-1];
                                }
                                else if(line.contains("Failed password")){
                                    ip = parts[parts.length-4];
                                }
                                else{
                                    ip = "";
                                    System.out.println("ERROR TAKING IP (no matches found)");
                                }

                                if(!ip.equals("")){
                                    occurrences.addOccurrence(ip, new SimpleDateFormat(format).format(date));
                                }

                            }
                                   
                            
                        }
                    }
                  
                }
               br.close();
            }catch(Exception ex){
                System.out.println("Error in occurrencessearch method search: "+ex.toString());
            }
        }
        
        //Return the list with the IPs with the equal or greater number of occurrences
        return occurrences.occurrencesWithNumberAboveOrEqual(number);
    }
    
    
    public OccurrencesList searchOccurrencesWindows(int number, int seconds) throws FileNotFoundException, IOException{
        OccurrencesList occurrences = new OccurrencesList();
        
        if(file != null){
            //Opening the file
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line, ip, format = "yyyy MMM dd HH:mm:ss";
                Date date, current_date, last_cycle;
                Calendar calendar;
                String [] parts;
                
                
                current_date = Calendar.getInstance().getTime();                
                calendar = Calendar.getInstance();
                calendar.setTime(current_date);

                //Then we take the current year
                String year = Integer.toString(calendar.get(Calendar.YEAR));
                
                //For the first cycle we need last_cycle
                last_cycle = new Date(current_date.getTime()-seconds*1000); 
                    
                //Looking for valid IPs at the time given
                while ((line = br.readLine()) != null) {
                    
                    //System.out.println(line);
              
                    if(line.contains("Invalid user") ||    //Invalid user
                            line.contains("Failed password")){     //Valid user but invalid password (3 times)
                        //Checking date and time
                        parts = line.split(" ");

                        //System.out.println("DENTRO DE IF INVALID USER OR FAILED PASSWORD");
                        //First we take the month, day and time
                        //String month;
                        String day, time, month;

                        year=parts[1].substring(0,4);
                        month=parts[1].substring(5,7);
                        day=parts[1].substring(8,10);
                        time=parts[2].substring(0, parts[2].length()-4);

                        switch(month){
                            case "01":
                                month = "Ene";
                                break;
                            case "02":
                                month = "Feb";
                                break;
                            case "03":
                                month = "Mar";
                                break;
                            case "04":
                                month = "Abr";
                                break;
                            case "05":
                                month = "May";
                                break;
                            case "06":
                                month = "Jun";
                                break;
                            case "07":
                                month = "Jul";
                                break;
                            case "08":
                                month = "Ago";
                                break;
                            case "09":
                                month = "Sep";
                                break;
                            case "10":
                                month = "Oct";
                                break;
                            case "11":
                                month = "Nov";
                                break;
                            case "12":
                                month = "Dic";
                                break;
                        }
                        //System.out.println("DESPUES DE SWITCH DE MESES: "+year+" "+month+" "+day+" "+time);
                        date = new SimpleDateFormat(format).parse(year+" "+month+" "+day+" "+time);

                        //If the date with the current year is after the current date 
                        //(when the current year changed), we take the last year before this one
                        /*if(date.after(current_date)){
                            year = Integer.toString(calendar.get(Calendar.YEAR)-1);
                            date = new SimpleDateFormat(format).parse(year+" "+month+" "+day+" "+time);
                        }*/

                        //Finally, we compare if the new date and time are between this cycle
                        //and the last one

                        if(date.after(last_cycle) && date.before(current_date)){
                            //Taking the IP
                            if(line.contains("Invalid user")){
                                ip = parts[parts.length-3];
                                //System.out.println("IP INVALID:"+ip);
                            }
                            else if(line.contains("Failed password")){
                                ip = parts[parts.length-4];
                                //System.out.println("IP: "+ip);
                            }
                            else{
                                ip = "";
                                System.out.println("ERROR TAKING IP (no matches found)");
                            }
                            
                            if(!ip.equals("")){
                                occurrences.addOccurrence(ip, new SimpleDateFormat(format).format(date));
                                //System.out.println("IP: "+ip);
                            }

                        }

                    }
  
                }
                br.close();
            }catch(Exception ex){
                System.out.println("Error in occurrencessearch method search: "+ex.toString());
            }
        }
        
        //Return the list with the IPs with the equal or greater number of occurrences
        return occurrences.occurrencesWithNumberAboveOrEqual(number);
    }
    
}


