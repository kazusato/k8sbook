package k8sbook.sampleapp.domain.service;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import k8sbook.sampleapp.domain.model.Location;
import k8sbook.sampleapp.domain.model.Region;
import org.assertj.db.type.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

import static com.ninja_squad.dbsetup.Operations.*;
import static org.assertj.db.api.Assertions.assertThat;

@SpringBootTest
public class LocationServiceTest {

    @Autowired
    private LocationService service;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Test
    @Tag("DBRequired")
    public void testRegisterLocations() {
        var locationList = List.of(
                new Location("地点5", new Region(1, "地域1", LocalDateTime.now()), "地点5の詳細です。"),
                new Location("地点6", new Region(1, "地域1", LocalDateTime.now()), "地点6の詳細です。")
        );
        service.registerLocations(locationList);

        var locationTable = new Table(dataSource, "location");
        assertThat(locationTable).hasNumberOfRows(6);
    }

    @BeforeEach
    public void prepareDatabase() {
        var operations = sequenceOf(
                deleteAllFrom("location"),
                deleteAllFrom("region"),
                insertInto("region")
                        .columns("region_id", "region_name", "creation_timestamp")
                        .values(1, "地域1", LocalDateTime.now())
                        .values(2, "地域2", LocalDateTime.now())
                        .values(3, "地域3", LocalDateTime.now())
                        .values(4, "地域4", LocalDateTime.now())
                        .build(),
                insertInto("location")
                        .columns("location_id", "location_name", "region_id", "note")
                        .values(1, "地点1", 1, "地点1の詳細です。")
                        .values(2, "地点2", 1, "地点2の詳細です。")
                        .values(3, "地点3", 1, "地点3の詳細です。")
                        .values(4, "地点4", 1, "地点4の詳細です。")
                        .build()
        );
        var dbSetup = new DbSetup(new DataSourceDestination(dataSource), operations);
        dbSetup.launch();
    }

}
