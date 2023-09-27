package tg.kindhands_bot.kindhands.services;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import tg.kindhands_bot.kindhands.components.MessagesBotFromControllers;
import tg.kindhands_bot.kindhands.entities.Animal;
import tg.kindhands_bot.kindhands.entities.User;
import tg.kindhands_bot.kindhands.entities.tamed.TamedAnimal;
import tg.kindhands_bot.kindhands.entities.tamed.TamedCat;
import tg.kindhands_bot.kindhands.entities.tamed.TamedDog;
import tg.kindhands_bot.kindhands.repositories.AnimalsRepository;
import tg.kindhands_bot.kindhands.repositories.UserRepository;
import tg.kindhands_bot.kindhands.repositories.tamed.TamedAnimalRepository;

import java.time.LocalDate;
import java.util.Collection;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AnimalsRepository animalsRepository;
    private final TamedAnimalRepository tamedAnimalRepository;

    private final MessagesBotFromControllers messagesBot;

    public UserService(UserRepository userRepository,
                       AnimalsRepository animalsRepository,
                       TamedAnimalRepository tamedAnimalRepository,
                       MessagesBotFromControllers messagesBot) {
        this.userRepository = userRepository;
        this.animalsRepository = animalsRepository;
        this.tamedAnimalRepository = tamedAnimalRepository;
        this.messagesBot = messagesBot;
    }

    /**
     * Метод добавления пользователя в Blacklist
     * -----||-----
     * add user in Blacklist method
     */

    public String addUserBlacklist(Long id, String messageBlock) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new NullPointerException("Пользователь с id '" + id + "' не найден.");
        }
        user.setBlocked(true);
        user.setDenialReason(messageBlock);
        userRepository.save(user);

        messagesBot.sendMessageUser(user, "Уважаемый " + user.getFirstName() + " " + user.getPatronymic() +
                "\nВы были заблокированы по причине: " + user.getDenialReason() +
                "\n\nЕсли произошла ошибка или вы хотите оспорить данное решение, то обратитесь к нашим волонтерам." +
                "\n\nВсего доброго!");

        return "Пользователь " + user.getLastName() + " " + user.getFirstName() + " " + user.getPatronymic() + " добавлен в черный список";
    }

    /**
     * Добавляет пользователю, взятое им, животное.
     * -----||-----
     * Adds an animal taken by the user.
     */
    public String addUserAnimal(@PathVariable Long idUser, @RequestParam Long idAnimal) {
        User user = userRepository.findById(idUser).orElse(null);
        if (user == null) {
            throw new NullPointerException("Пользователь с id '" + idUser + "' не найден");
        }

        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            return "Пользователю " + user.getFirstName() + " необходимо, через бота, заполнить контактные данные.";
        }

        Animal animal = animalsRepository.findById(idAnimal).orElse(null);
        if (animal == null) {
            throw new NullPointerException("Животное с id '" + idUser + "' не найдено");
        }

        LocalDate nowDate = LocalDate.now();

        TamedAnimal tamedAnimal;
        switch (animal.getTypeAnimal()) {
            case CAT: {
                tamedAnimal = new TamedCat();
                break;
            }
            case DOG: {
                tamedAnimal = new TamedDog();
                break;
            }
            default: return "Не реализован функционал, для данного вида животного.";
        }
        tamedAnimal.setUser(user);
        tamedAnimal.setAnimal(animal);
        tamedAnimal.setDateAdoption(nowDate);
        tamedAnimal.setDateLastReport(nowDate);
        tamedAnimalRepository.save(tamedAnimal);

        messagesBot.sendMessageUser(user, "Уважаемый " + user.getFirstName() + " " + user.getPatronymic() + ", поздравляем Вас с новым членом семьи!" +
                "\nНадеемся, что " + animal.getName() + " принесет вам только положительные эмоции." +
                "\n\nНапоминаем! Каждый день, начиная со следующего дня, в течении 30 дней необходимо отправлять отчеты " +
                "(после выбора приюта нажать на кнопку \"Отправить отчет\")." +
                "\nТакже срок отправки отчетов может быть продлен, об этом Вам придет соответствующее сообщение в Боте." +
                "\nВ случае возникновения вопросов или неполадок в работе Бота, вы всегда можете обратиться к нашим волонтерам." +
                "\n\nСпасибо за то, что делаете мир добрее!");

        return "Пользователю " + user.getLastName() + " " + user.getFirstName() + " " + user.getPatronymic() + " добавлено животное " + animal.getName();
    }

    /**
     * Продлевает испытательный срок пользователю.
     * -----||-----
     * Extends the probation period to the user.
     */
    public String extendProbationPeriod(Long id, Integer term) {
        TamedAnimal tamedAnimal = tamedAnimalRepository.findByUser_Id(id);
        if (tamedAnimal == null) {
            throw new NullPointerException("Пользователь с id '" + id + "' не найден или не приручал животное");
        }
        User user = tamedAnimal.getUser();
        tamedAnimal.setNumReports(tamedAnimal.getNumReports() + term);
        tamedAnimalRepository.save(tamedAnimal);

        messagesBot.sendMessageUser(user, "Уважаемый " + user.getFirstName() + " " + user.getPatronymic() +
                ", вам был продлен испытательный срок на " + term + " дней.\n" +
                "За дополнительной информацией вы можете обратиться к волонтерам." +
                "Спасибо!");

        return "Пользователю " + user.getLastName() + " " + user.getFirstName() + " " + user.getPatronymic() + ", был продлен " +
                "испытательный срок.";
    }

    /**
     * Метод полученя всех пользователей, запросивших  помощь волонтера
     * -----||-----
     * Method of getting all users who requested the help of a volunteer
     */
    public Collection<User> needVolunteerHelper() {
        return userRepository.findByNeedHelpTrue();
    }

    /**
     * Метод для изменения значения поля needHelp у пользователя после оказания помощи
     * -----||-----
     * Method for changing the value of the user's needHelp field
     */
    public String isNeedHelp(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new NullPointerException("Пользователь с id '" + id + "' не найден.");
        }
        user.setNeedHelp(false);
        userRepository.save(user);

        messagesBot.sendMessageUser(user, user.getFirstName() + " " + user.getPatronymic() + ", Ваша проблема" +
                "была решена. Если это не так, пожалуйста, повторите попытку.");

        return "Проблема пользователя " + user.getLastName() + " " + user.getFirstName() + " " + user.getPatronymic() + " решена";//добавить фио
    }
}
