package quiz.bot.dao

import cats.effect.Async
import doobie.implicits._
import doobie.quill.DoobieContext
import doobie.util.transactor.Transactor
import io.getquill.{idiom => _, _}
import quiz.bot.dao.DaoModel.Question

class CategoryDao[F[_]](transactor: Transactor[F])(implicit F: Async[F]) {
  private val dc = new DoobieContext.Postgres(Literal)
  import dc._

  val mockQuestions = List(
    Question("Добро пожаловать. Настала пора оценить твои познания в настолках. И начнем мы с самого простого вопроса: какая из твоих игр является самой рейтинговой?", "7 чудес"),
    Question("Мои поздравления, ты справилась с первым заданием, однако дальше будет не так просто. Назови самый густонаселенный город в пандемии.", "джакарта"),
    Question("Неплохо-неплохо, a какой персонаж раньше всего заканчивает игру в маскараде?", "жулик"),
    Question("Твое любимое: как можно продаться в рабство турбобелкам?", "подлижись"),
    Question("Стекло/няня, орел/номер, медведь/парашут, вор/?", "лошадь"),
    Question("В каком году запустили board games geek?", "2000"),
    Question("Хмм, пока ты справляешься, но это даже не середина пути. Где ты можешь обменять овцу на камень?", "колонизаторы"),
    Question("Что может выломать заколоченную дверь?", "топор"),
    Question("Кто может уничтожить еду из кормовой базы?", "топотун"),
    Question("А теперь один из самых глупых вопросов: кто в готем сити не ограничен в количестве?", "полицейский"),
    Question("Как зовут подозреваемого с длинными волосами, щетиной и круглыми очками?", "нейл"),
    Question("Назови имя гения, который получает по 1000 долларов за кажду проданную акцию?", "грэм"),
    Question("Какой лидер династии может вербовать двух воинов вместо одного?", "избранник"),
    Question("А в какой стране можно больше всего заработать?", "лихтенштейн"),
    Question("Осталось немножко. Какую максимальную сумму можно собрать, убегаю с острова?", "28"),
    Question("А кто может за три монеты купить мрамор?", "торговка"),
    Question("Сколько тысяч долларов можно заработать в криминальном мире?", "425")
  )

  def getQuestionsFromDb: F[List[Question]] =
    run(quote{ query[Question] }).transact[F](transactor)

  def getQuestions: F[List[Question]] =
    F.delay(mockQuestions)
    //run(quote{ query[Question] }).transact[F](transactor)
}

