package shop.readmecorp.adminserverreadme.modules.history.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.readmecorp.adminserverreadme.modules.history.entity.ViewHistory;

public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Integer> {
}
