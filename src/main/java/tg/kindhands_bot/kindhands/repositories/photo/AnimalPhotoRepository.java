package tg.kindhands_bot.kindhands.repositories.photo;

import org.springframework.data.jpa.repository.JpaRepository;
import tg.kindhands_bot.kindhands.entities.photo.AnimalPhoto;

public interface AnimalPhotoRepository extends JpaRepository<AnimalPhoto, Long> {
}
