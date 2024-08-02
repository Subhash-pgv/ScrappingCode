package WebScrapers;

import java.io.IOException;
import java.sql.SQLException;

public class MainCoordinator {
    public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException, SQLException {
       
        WebScrapers.JobScrapping1.main(args);
        WebScrapers.JobScrapping2.main(args);
        WebScrapers.JobScrapping3.main(args);
        WebScrapers.JobScrapping4.main(args);

    }
}
