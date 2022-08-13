package com.kanpo.trial;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kanpo.trial.exception.BadRequestException;
import com.kanpo.trial.exception.InternalServerException;
import com.kanpo.trial.log.MyLogger;
import com.kanpo.trial.model.Analysis;
import com.kanpo.trial.model.Answer;
import com.kanpo.trial.model.Medicine;
import com.kanpo.trial.model.Question;
import com.kanpo.trial.model.QuestionOption;
import com.kanpo.trial.repository.AnalysisRepository;
import com.kanpo.trial.repository.AnswerRepository;
import com.kanpo.trial.repository.MedicineRepository;
import com.kanpo.trial.repository.QuestionOptionRepository;
import com.kanpo.trial.repository.QuestionRepository;
import com.kanpo.trial.restRequest.SendAnswerRequest;
import com.kanpo.trial.restResponse.NextQuestion;

/**
* RESTのコントローラークラス
* @author　keita
*/
@RestController
public class KanpoRestController {

	/** 最初の質問オブジェクトのID */
	public static long topQuestionId = -1;

	@Autowired
	AnalysisRepository analysisRepository;

	@Autowired
	AnswerRepository answerRepository;

	@Autowired
	MedicineRepository medicineRepository;

	@Autowired
	QuestionOptionRepository questionOptionRepository;

	@Autowired
	QuestionRepository questionRepository;

	/**
	 * /startAnalysis APIの処理メソッド
	 *
	 * @return TOPの質問を返却します
	 * @throws Exception 例外
	 */
	@RequestMapping("/startAnalysis")
	public NextQuestion startAnalysis() throws Exception {
		try {
			// TOPの質問を取得する
			Optional<Question> topQuestion = questionRepository.findById(topQuestionId);
			Analysis analysis = new Analysis(topQuestion.get());
			analysisRepository.saveAndFlush(analysis);
			MyLogger.info("Analysis data created successfully analysisId={0}", analysis.getId());

			return new NextQuestion(analysis.getId()
					, analysis.getNowQuestion().getQuestionContent()
					, analysis.getNowQuestion().getOptionList()
					, true);
		} catch (Exception e) {
			// 例外が発生した場合はそのままスローしハンドラーに任せる
			MyLogger.error(e);
			throw e;
		}
	}

	/**
	 * /sendAnswer APIの処理メソッド
	 *
	 * @param request 質問への回答
	 * @return 次の質問を返却します
	 * @throws Exception 例外
	 */
	@RequestMapping(value="/sendAnswer", method=RequestMethod.POST)
	public NextQuestion sendAnswer(
			@RequestBody SendAnswerRequest request) throws Exception {

		try {
			// bodyパラメーターログ出力
			request.outputLog();
			// analysisのデータをanalysisIdから参照して情報を取得する
			Optional<Analysis> analysisOpt = analysisRepository.findById(request.analysisId);
			// analysisIdに対応するanalysisが存在しない場合は例外送出
			if (!analysisOpt.isPresent()) {
				throw new BadRequestException("There is no analysis corresponding to analysisId");
			}
			Analysis analysis = analysisOpt.get();
			// analysis.statusが既にENDになっている場合は、分析が完了しているので、エラー
			if (analysis.getStatus() == Analysis.END) {
				throw new BadRequestException("Analysis has already been completed");
			}

			// 現在のQuestionオブジェクトを取得する
			Question nowQuestion = analysis.getNowQuestion();
			// QuestionオブジェクトのoptionListと比較して存在しない選択番号が入力された場合はエラー
			if (request.answerNum < 0 || request.answerNum >= nowQuestion.getOptionList().size()) {
				throw new BadRequestException("Illegal answerNum was selected");
			}

			// 解答番号に合致する選択情報を取得する
			QuestionOption queOpt = nowQuestion.getOptionList().get(request.answerNum);
			// 次が質問か解答かをチェックする
			Question nextQuestion = null;
			if (queOpt.getAnswerId() != -1) {
				// answerIdに対応する解答情報を取得する
				Optional<Answer> answerOpt;
				answerOpt = answerRepository.findById(queOpt.getAnswerId());
				if (!answerOpt.isPresent()) {
					throw new InternalServerException("Failed to get answer");
				}
				MyLogger.info("Successfully obtained answer");

				Answer answer = answerOpt.get();
				analysis.setAnswerId(answer.getId());
				analysis.setStatus(Analysis.END);
				analysisRepository.saveAndFlush(analysis);
				return new NextQuestion(analysis.getId(), false);
			}

			// questionIdに対応する次の質問情報を取得する
			Optional<Question> optQue;
			optQue = questionRepository.findById(queOpt.getQuestionId());
			if (!optQue.isPresent()) {
					throw new InternalServerException("Failed to get next question");
			}
			nextQuestion = optQue.get();
			MyLogger.info("Successfully obtained next question");

			// 分析のnowQuestionに返却する質問を設定
			analysis.setNowQuestion(nextQuestion);
			analysisRepository.saveAndFlush(analysis);

			return new NextQuestion(analysis.getId()
					, nextQuestion.getQuestionContent()
					, nextQuestion.getOptionList()
					, true);
		} catch (Exception e) {
			MyLogger.error(e);
			throw e;
		}
	}

	/**
	 * /backQuestion APIの処理メソッド
	 *
	 * @param analysisId 解析ID
	 * @return 前の質問を返却します
	 * @throws Exception 例外
	 */
	@RequestMapping(value = "/backQuestion/{analysisId}")
	public NextQuestion backQuestion(@PathVariable long analysisId) throws Exception {

		try {
			// analysisのデータをanalysisIdから参照して情報を取得する
			Optional<Analysis> analysisOpt = analysisRepository.findById(analysisId);
			// analysisIdに対応するanalysisが存在しない場合は例外送出
			if (!analysisOpt.isPresent()) {
				throw new BadRequestException("There is no analysis corresponding to analysisId");
			}
			Analysis analysis = analysisOpt.get();
			// analysis.statusが既にENDになっている場合は、分析が完了しているので、エラー
			if (analysis.getStatus() == Analysis.END) {
				throw new BadRequestException("Analysis has already been completed");
			}

			// 現在のQuestionオブジェクトを取得する
			// 前の質問が存在しない場合(例えばトップでの質問など）の場合はエラー
			Question nowQuestion = analysis.getNowQuestion();
			Question backQuestion = nowQuestion.getBackNode();
			if (backQuestion == null) {
				throw new BadRequestException("previous question does not exist");
			}

			// 分析のnowQuestionに返却する質問を設定
			analysis.setNowQuestion(backQuestion);
			analysisRepository.saveAndFlush(analysis);

			return new NextQuestion(analysis.getId()
					, backQuestion.getQuestionContent()
					, backQuestion.getOptionList()
					, true);
		} catch (Exception e) {
			MyLogger.error(e);
			throw e;
		}
	}

	/**
	 * /getResult APIの処理メソッド
	 *
	 * @param analysisId 解析ID
	 * @return 解析結果を返却します
	 * @throws Exception 例外
	 */
	@RequestMapping(value = "/getResult/{analysisId}")
	public List<Medicine> getResult(@PathVariable long analysisId)  throws Exception {

		try {
			// analysisのデータをanalysisIdから参照して情報を取得する
			Optional<Analysis> analysisOpt = analysisRepository.findById(analysisId);
			// analysisIdに対応するanalysisが存在しない場合は例外送出
			if (!analysisOpt.isPresent()) {
				throw new BadRequestException("There is no analysis corresponding to analysisId");
			}
			Analysis analysis = analysisOpt.get();
			// analysis.statusが既にENDになっている場合は、分析が完了しているので、エラー
			if (analysis.getStatus() == Analysis.CONTINUE) {
				throw new BadRequestException("Analysis not yet completed");
			}

			// analysisIdからresultIdを取得する
			long answerId = analysis.getAnswerId();
			Optional<Answer> answerOpt = answerRepository.findById(answerId);
			if (!answerOpt.isPresent()) {
				throw new InternalServerException("Failed to get answer");
			}
			return answerOpt.get().getMedicineList();
		} catch (Exception e) {
			MyLogger.error(e);
			throw e;
		}
	}

	/**
	 * アプリケーションの初期化メソッド
	 */
	@PostConstruct
	public void init() {
		// 漢方についてのデータを登録しておく
		Medicine med1 = new Medicine();
		med1.setName("漢方1");
		med1.setDetailInfo("漢方1についての説明です");
		med1.setImagePath("img01.jpeg");
		medicineRepository.saveAndFlush(med1);
		Medicine med2 = new Medicine();
		med2.setName("漢方2");
		med2.setDetailInfo("漢方2についての説明です");
		med2.setImagePath("img03.jpg");
		medicineRepository.saveAndFlush(med2);
		Medicine med3 = new Medicine();
		med3.setName("漢方3");
		med3.setDetailInfo("漢方3についての説明です");
		med3.setImagePath("img04.jpeg");
		medicineRepository.saveAndFlush(med3);

		// 解答についてデータを登録しておく
		Answer ans1 = new Answer();
		ArrayList<Medicine> medicineList = new ArrayList<>();
		medicineList.add(med1);
		medicineList.add(med2);
		medicineList.add(med3);
		ans1.setMedicineList(medicineList);
		answerRepository.saveAndFlush(ans1);

		// 質問についてデータを登録しておく
		Question que1 = new Question();
		que1.setQuestionContent("以下から選択しなさい");
		ArrayList<QuestionOption> optionList = new ArrayList<>();
		QuestionOption option1 = new QuestionOption("選択Aです", ans1);
		QuestionOption option2 = new QuestionOption("選択Bです", ans1);
		QuestionOption option3 = new QuestionOption("選択Cです", ans1);
		QuestionOption option4 = new QuestionOption("選択Dです", ans1);
		questionOptionRepository.saveAndFlush(option1);
		questionOptionRepository.saveAndFlush(option2);
		questionOptionRepository.saveAndFlush(option3);
		questionOptionRepository.saveAndFlush(option4);
		optionList.add(option1);
		optionList.add(option2);
		optionList.add(option3);
		optionList.add(option4);
		que1.setOptionList(optionList);
		questionRepository.saveAndFlush(que1);

		Question que2 = new Question();
		que2.setQuestionContent("トップ質問: 以下から選択しなさい");
		ArrayList<QuestionOption> optionList2 = new ArrayList<>();
		QuestionOption option5 = new QuestionOption("選択Eです", que1);
		QuestionOption option6 = new QuestionOption("選択Fです", ans1);
		QuestionOption option7 = new QuestionOption("選択Gです", ans1);
		QuestionOption option8 = new QuestionOption("選択Hです", ans1);
		questionOptionRepository.saveAndFlush(option5);
		questionOptionRepository.saveAndFlush(option6);
		questionOptionRepository.saveAndFlush(option7);
		questionOptionRepository.saveAndFlush(option8);
		optionList2.add(option5);
		optionList2.add(option6);
		optionList2.add(option7);
		optionList2.add(option8);
		que2.setOptionList(optionList2);
		questionRepository.saveAndFlush(que2);

		// que1の親ノードにque2を登録する
		que1.setBackNode(que2);
		questionRepository.saveAndFlush(que1);

		topQuestionId = que2.getId();

		return;
	}
}