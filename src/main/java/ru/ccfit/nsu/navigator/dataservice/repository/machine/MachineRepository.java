
package ru.ccfit.nsu.navigator.dataservice.repository.machine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.Machine;

import java.util.Optional;

public interface MachineRepository extends JpaRepository<Machine, Long> {

    Optional<Machine> findByNumberAndNameAndTypeId(
            Long number,
            String name,
            Short typeId
    );

    @Modifying
    @Query(value = """
            INSERT INTO machine (number, name, type_id)
            VALUES (:number, :name, :typeId)
            ON CONFLICT ON CONSTRAINT uq_machine_identity
            DO NOTHING
            """, nativeQuery = true)
    int insertIfMissing(
            @Param("number") Long number,
            @Param("name") String name,
            @Param("typeId") Short typeId
    );
}
