package WebScrapers;

import java.io.IOException;
import java.sql.SQLException;

public class MainCoordinator {
    public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException, SQLException {
       
        WebScrapers.JobScrapping2.main(args);
        WebScrapers.JobScrapping1.main(args);

    }
}
