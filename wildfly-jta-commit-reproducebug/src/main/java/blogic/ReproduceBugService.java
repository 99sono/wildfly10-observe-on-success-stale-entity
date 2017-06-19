package blogic;

import java.util.Random;

import javax.inject.Inject;

import db.crud.SomeEntityRepository;
import db.model.SomeEntity;

/**
 * Convenience tool to hammer the database.
 */
public class ReproduceBugService {

    @Inject
    SomeEntityRepository someEntityRepository;

    /**
     * Persists the desired given number of entities. Each entity persisted is given dynamic string text compoesed of a
     * random int and an increasing loop number.
     *
     * @param numberOfEntitiesToCreate
     *            the number of entities to be persisted - this variable may or may influence the likelihood of
     *            reproducing the bug being verified.
     */
    public void createSomeEntityEntities(int numberOfEntitiesToCreate) {
        int randomInt = new Random().nextInt();
        for (int i = 0; i < Math.max(numberOfEntitiesToCreate, 0); i++) {
            String entityText = String.format("PersistLoop (RandomInt x loopIteration) = (%1$d,%2$d)", randomInt, i);
            SomeEntity someEntity = new SomeEntity();
            someEntity.setText(entityText);
            someEntityRepository.perist(someEntity);
        }
    }

}
