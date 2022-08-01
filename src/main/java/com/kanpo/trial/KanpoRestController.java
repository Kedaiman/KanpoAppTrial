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

@RestController
public class KanpoRestController {

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

	@RequestMapping("/startAnalysis")
	public NextQuestion startAnalysis() {
		// TOPの質問を取得する
		Optional<Question> topQuestion = questionRepository.findById(topQuestionId);
		Analysis analysis = new Analysis(topQuestion.get());
		analysisRepository.saveAndFlush(analysis);
		return new NextQuestion(analysis.getId()
				, analysis.getNowQuestion().getQuestionContent()
				, analysis.getNowQuestion().getOptionList()
				, true);
	}

	@RequestMapping(value="/sendAnswer", method=RequestMethod.POST)
	public NextQuestion sendAnswer(
			@RequestBody SendAnswerRequest request) throws BadRequestException {

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
				throw new BadRequestException("Internal Server Error");
			}
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
				throw new BadRequestException("Internal Server Error");
		}
		nextQuestion = optQue.get();
		return new NextQuestion(analysis.getId()
				, nextQuestion.getQuestionContent()
				, nextQuestion.getOptionList()
				, true);
	}

	@RequestMapping(value = "/getResult/{analysisId}")
	public List<Medicine> getResult(@PathVariable long analysisId)  throws BadRequestException {
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
			throw new BadRequestException("Internal Server Error");
		}
		return answerOpt.get().getMedicineList();
	}

	@PostConstruct
	public void init() {
		// 漢方についてのデータを登録しておく
		Medicine med1 = new Medicine();
		med1.setName("漢方1");
		med1.setDetailInfo("漢方1についての説明です");
		medicineRepository.saveAndFlush(med1);
		Medicine med2 = new Medicine();
		med2.setName("漢方2");
		med2.setDetailInfo("漢方2についての説明です");
		medicineRepository.saveAndFlush(med2);
		Medicine med3 = new Medicine();
		med3.setName("漢方3");
		med3.setDetailInfo("漢方3についての説明です");
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

		topQuestionId = que2.getId();

		return;
	}
}