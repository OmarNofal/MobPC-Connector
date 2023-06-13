package com.omar.pcconnector.ui.main

//
//@Composable
//fun MainApp(
//    eventsFlow: MutableSharedFlow<ApplicationEvent>
//) {
//
//
//
//    Box(contentAlignment = Alignment.TopCenter) {
//
//
//        FileSystemUI(
//            Modifier
//                .padding(top = 56.dp)
//                .fillMaxSize()
//                , hiltViewModel(),
//            eventsFlow
//        )
//
//
//
//            MainToolbar(
//                onShowTransfers = { transfersShown = !transfersShown },
//                isTransferOngoing = transfers.any {
//                    (it.transferState is TransferState.Running) or (it.transferState is TransferState.Initializing)
//                }
//            )
//            //Spacer(modifier = Modifier.height(8.dp))
//
//            Box(modifier = Modifier.fillMaxSize()) {
//                TransferPopup(
//                    modifier =
//                    Modifier
//                        .fillMaxWidth(0.9f),
//                    visible = transfersShown, { transfersShown = false },
//                    onCancel = transfersViewModel::cancelTransfer,
//                    onDelete = transfersViewModel::deleteTransfer,
//                    transferViewModel = transfersViewModel
//                )
//            }
//
//    }
//
//}