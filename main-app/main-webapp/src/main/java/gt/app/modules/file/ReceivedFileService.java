package gt.app.modules.file;

import gt.app.domain.ReceivedFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceivedFileService {

    final ReceivedFileRepository receivedFileRepository;

    @Transactional(readOnly = true)
    public Optional<ReceivedFile> findById(UUID id) {
        return receivedFileRepository.findById(id);
    }
}
